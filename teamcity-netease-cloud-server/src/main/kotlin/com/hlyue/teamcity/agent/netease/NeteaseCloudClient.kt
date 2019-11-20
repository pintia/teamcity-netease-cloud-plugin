package com.hlyue.teamcity.agent.netease

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.Gson
import com.hlyue.teamcity.agent.netease.api.StatefulWorkloadCreateResponse
import com.hlyue.teamcity.agent.netease.other.NameGenerator
import jetbrains.buildServer.clouds.*
import jetbrains.buildServer.serverSide.AgentDescription
import kotlinx.coroutines.*
import org.json.JSONObject

class NeteaseCloudClient(
  private val profileId: String,
  private val cloudClientParameters: CloudClientParameters,
  private val serverUrl: String
) : CloudClientEx {

  companion object : Constants()
  // 15 seconds * 12 = 3 minutes. This usually means creation failed.
  private val MAX_ERROR_COUNT = 20

  val instances = mutableListOf<NeteaseCloudInstance>()

  private val gson = Gson()
  private val image = NeteaseCloudImage(cloudClientParameters.getParameter(PREFERENCE_MACHINE_TYPE)!!, this)
  private val connector = NeteaseOpenApiConnector(
    cloudClientParameters.getParameter(PREFERENCE_ACCESS_KEY)!!,
    cloudClientParameters.getParameter(PREFERENCE_ACCESS_SECRET)!!
  )
  private val diskProvider = NeteaseDiskProvider(profileId, connector)
  private val logger = Constants.buildLogger()
  private val backgroundJob: Job
  private var lastError: CloudErrorInfo? = null

  init {
    backgroundJob = GlobalScope.launch {
      var responseString = ""
      while (isActive) {
        try {
          delay(15 * 1000)
          val config = NeteaseConfig.buildFromCloudConfig(cloudClientParameters)
          responseString = connector.NeteaseOpenApiRequestBuilder(
            action = "DescribeStatefulWorkloads",
            serviceName = "ncs",
            version = "2017-11-16",
            query = mapOf(
              "NamespaceId" to config.namespaceId.toString()
            )
          ).request()
          val response = JSONObject(responseString).getJSONArray("StatefulWorkloads")
          val workloadIds = mutableListOf<Long>()
          response.forEach {
            val item = it as JSONObject
            val id = item.getLong("StatefulWorkloadId")
            workloadIds.add(id)
            val name = item.getString("Name")
            if (!name.startsWith("tc-")) {
              return@forEach
            }
            val instance = instances.firstOrNull { it.workloadId == id }
            if (instance != null) {
              instance.setStatus(item.getString("Status"))
              if (instance.errorCount >= MAX_ERROR_COUNT) {
                terminateInstanceAsync(instance)
              }
            }
          }
          instances.removeAll {
            // Not created by netease, or we terminated it before.
            // we can remove it here.
            !workloadIds.contains(it.workloadId)
          }
        } catch (e: Exception) {
          lastError = CloudErrorInfo("backgroudJob", responseString, e)
        }
      }
    }
  }

  override fun findInstanceByAgent(agent: AgentDescription): CloudInstance? {
    return instances.firstOrNull { agent.configurationParameters[ENV_INSTANCE_ID] == it.envWorkloadId }
  }

  override fun getErrorInfo(): CloudErrorInfo? {
    return lastError
  }

  override fun findImageById(imageId: String): CloudImage? {
    return if (imageId == image.id) image else null
  }

  override fun canStartNewInstance(image: CloudImage): Boolean {
    return instances.size < 10
  }

  override fun isInitialized(): Boolean {
    return true
  }

  override fun generateAgentName(agent: AgentDescription): String? {
    return agent.configurationParameters[ENV_INSTANCE_NAME]
  }

  override fun getImages(): Collection<CloudImage> {
    return listOf(image)
  }

  override fun startNewInstance(image: CloudImage, tag: CloudInstanceUserData): NeteaseCloudInstance = runBlocking {
    lastError = null
    var response: String? = null

    try {
      val config = NeteaseConfig.buildFromCloudConfig(cloudClientParameters)
      val myImage = image as NeteaseCloudImage
      val name = "tc-" + NameGenerator.generate()
      val dockerDiskId = diskProvider.getDockerDisk(config)
      val instance = NeteaseCloudInstance(name, myImage, connector, config)

      val request = jsonObject(
        "Placement" to jsonObject(
          "ZoneId" to jsonArray("cn-east-1b")
        ),
        "SpecType" to config.machineType,
        "VirtualPrivateCloud" to jsonObject(
          "VpcId" to config.vpcId,
          "SubnetId" to config.subnetId
        ),
        "SecurityGroupIds" to jsonArray(config.securityGroupId),
        "Labels" to jsonObject(
          "tc-agent" to "netease",
          "agent-id" to instance.envWorkloadId,
          "profile-id" to profileId
        ),
        "ContainerType" to "Standard",
        "NamespaceId" to config.namespaceId,
        "Name" to name,
        "Containers" to jsonArray(
          jsonObject(
            "Name" to name,
            "Image" to "hub.c.163.com/patest/teamcity-agent:${config.imageTag}",
            "LogDirs" to jsonArray("/opt/buildagent/logs/"),
            "ResourceRequirements" to jsonObject(
              "Limits" to REQUIREMENTS[config.machineType],
              "Requests" to REQUIREMENTS[config.machineType]
            ),
            "Envs" to jsonArray(
              jsonObject(
                "Name" to ENV_SERVER_URL,
                "Value" to serverUrl
              ),
              jsonObject(
                "Name" to ENV_INSTANCE_ID,
                "Value" to instance.envWorkloadId
              ),
              jsonObject(
                "Name" to ENV_NETEASE_TC_AGENT,
                "Value" to "true"
              ),
              jsonObject(
                "Name" to "DOCKER_IN_DOCKER",
                "Value" to "start"
              ),
              jsonObject(
                "Name" to ENV_INSTANCE_NAME,
                "Value" to name
              ),
              jsonObject(
                "Name" to ENV_YARN_CACHE_FOLDER,
                "Value" to ENV_YARN_CACHE_FOLDER_VALUE

              ),
              jsonObject(
                "Name" to ENV_GRADLE_USER_HOME,
                "Value" to ENV_GRADLE_USER_HOME_VALUE
              ),
              jsonObject(
                "Name" to ENV_AGENT_OPTS,
                "Value" to ENV_AGENT_OPTS_VALUE
              )
            ),
            "SecurityContext" to jsonObject(
              "Privilege" to true
            ),
            "DataDisks" to jsonArray(jsonObject(
              "DiskType" to NeteaseDiskProvider.DISK_TYPE,
              "MountPath" to "/var/lib/docker/",
              "DiskId" to dockerDiskId
            ))
          )
        )
      )
      response = connector.NeteaseOpenApiRequestBuilder(
        action = "CreateStatefulWorkload",
        method = "POST",
        serviceName = "ncs",
        version = "2017-11-16",
        data = request.toString()
      ).request()
      val json = gson.fromJson(response, StatefulWorkloadCreateResponse::class.java)
      instance.workloadId = json.StatefulWorkloadId

      instances.add(instance)
      myImage.instances.add(instance)
      instance
    } catch (e: Exception) {
      logger.infoAndDebugDetails("startNewInstance", e)
      lastError = CloudErrorInfo("cannot start new instant", response ?: "null response", e)
      instances.first()
    }
  }

  private suspend fun terminateInstanceAsync(instance: CloudInstance) {
    (instance as NeteaseCloudInstance).terminate()
  }

  override fun terminateInstance(instance: CloudInstance) = runBlocking {
    terminateInstanceAsync(instance)
  }

  override fun restartInstance(instance: CloudInstance) = runBlocking {
    (instance as NeteaseCloudInstance).forceRestart()
  }

  override fun dispose() = runBlocking {
    backgroundJob.cancel()
    instances.forEach {
      it.terminate()
    }
    instances.clear()
  }
}
