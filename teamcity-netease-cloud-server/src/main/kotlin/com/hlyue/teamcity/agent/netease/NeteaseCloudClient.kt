package com.hlyue.teamcity.agent.netease

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.Gson
import com.hlyue.teamcity.agent.netease.NeteaseCloudInstance.Companion
import com.hlyue.teamcity.agent.netease.api.StatefulWorkloadCreateRequest
import com.hlyue.teamcity.agent.netease.api.StatefulWorkloadCreateResponse
import com.hlyue.teamcity.agent.netease.api.WorkloadLabels
import jetbrains.buildServer.clouds.*
import jetbrains.buildServer.serverSide.AgentDescription
import jetbrains.buildServer.serverSide.ServerSettings
import kotlinx.coroutines.experimental.*
import org.apache.commons.lang3.RandomStringUtils
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class NeteaseCloudClient(
  private val profileId: String,
  private val cloudClientParameters: CloudClientParameters
) : CloudClientEx {

  companion object : Constants()

  val instances = mutableListOf<NeteaseCloudInstance>()

  private val gson = Gson()
  private val image = NeteaseCloudImage(cloudClientParameters.getParameter(PREFERENCE_MACHINE_TYPE)!!, this)
  private val connector = NeteaseOpenApiConnector(
    cloudClientParameters.getParameter(PREFERENCE_ACCESS_KEY)!!,
    cloudClientParameters.getParameter(PREFERENCE_ACCESS_SECRET)!!
  )
  private val diskProvider = NeteaseDiskProvider(profileId, connector)
  private val logger = Constants.buildLogger()
  private val context = newSingleThreadContext("NeteaseClient-${cloudClientParameters.profileDescription}")
  private val backgroundJob: Job
  private var lastError: CloudErrorInfo? = null

  init {
    backgroundJob = launch(context) {
      var responseString: String = ""
      while (true) {
        try {
          delay(15, TimeUnit.SECONDS)
          val config = NeteaseConfig.buildFromCloudConfig(cloudClientParameters)
          responseString = connector.NeteaseOpenApiRequestBuilder(
            action = "DescribeStatefulWorkloads",
            serviceName = "ncs",
            version = "2017-11-16",
            query = mapOf(
              "NamespaceId" to config.namespaceId.toString()
            )
          ).request().await()
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
            } else {
              // TODO: auto discover
//              val info = getInstanceInfo(config, id)
//              if (info != null) {
//                val label = gson.fromJson(info.getJSONObject("Labels").toString(), WorkloadLabels::class.java)
//                if (label.isAgent(profileId)) {
//                  // A new agent discovered. Typically when startup
//                  instances.add(NeteaseCloudInstance(info.getString("Name"), image, connector, config, 0).also {
//                    it.workloadId = id
//                    it.setStatus(info.getString("Status"))
//                  })
//                }
//              }
            }
          }
          instances.removeAll {
            val shouldRemove = !workloadIds.contains(it.workloadId)
            if (shouldRemove) {
              terminateInstance(it)
            }
            shouldRemove
          }
        } catch (e: Exception) {
          lastError = CloudErrorInfo("backgroudJob", responseString, e)
        }
      }
    }
  }

  private suspend fun getInstanceInfo(config: NeteaseConfig, workloadId: Long): JSONObject? {
    return try {
      val response = connector.NeteaseOpenApiRequestBuilder(
        action = "DescribeStatefulWorkloadInfo",
        version = "2017-11-16",
        serviceName = "ncs",
        query = mapOf(
          "NamespaceId" to config.namespaceId.toString(),
          "StatefulWorkloadId" to workloadId.toString()
        )
      ).request().await()
      JSONObject(response)
    } catch (e: Exception) {
      null
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
    return instances.size < 5
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

  override fun startNewInstance(image: CloudImage, tag: CloudInstanceUserData): NeteaseCloudInstance = runBlocking(context) {
    lastError = null
    var response: String? = null

    try {
      val config = NeteaseConfig.buildFromCloudConfig(cloudClientParameters)
      val myImage = image as NeteaseCloudImage
      val name = "tc-${RandomStringUtils.randomAlphabetic(8).toLowerCase()}"
      val disks = diskProvider.getDisks(name).await()
      val instance = NeteaseCloudInstance(name, myImage, connector, config, disks.first, disks.second)

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
            "Image" to "hub.c.163.com/patest/teamcity-agent:2018.1",
            "LogDirs" to jsonArray("/opt/buildagent/temp/", "/opt/buildagent/logs/"),
            "ResourceRequirements" to jsonObject(
              "Limits" to StatefulWorkloadCreateRequest.REQUIREMENTS[config.machineType],
              "Requests" to StatefulWorkloadCreateRequest.REQUIREMENTS[config.machineType]
            ),
            "Envs" to jsonArray(
              jsonObject(
                "Name" to ENV_SERVER_URL,
                "Value" to config.serverUrl
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
              )
            ),
            "SecurityContext" to jsonObject(
              "Privilege" to true
            ),
            "DataDisks" to jsonArray(jsonObject(
              "DiskType" to "CloudSsd",
              "MountPath" to "/opt/",
              "DiskId" to instance.dataDiskId
            ), jsonObject(
              "DiskType" to "CloudSsd",
              "MountPath" to "/var/lib/docker/",
              "DiskId" to instance.dockerDiskId
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
      ).request().await()
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

  override fun terminateInstance(instance: CloudInstance) {
    (instance as NeteaseCloudInstance).let {
      it.terminate()
      diskProvider.removeDisk(it.dataDiskId)
      diskProvider.removeDisk(it.dockerDiskId)
      it.close()
    }
  }

  override fun restartInstance(instance: CloudInstance) {
    (instance as NeteaseCloudInstance).forceRestart()
  }

  override fun dispose() = runBlocking {
    logger.info("dispose:")
    backgroundJob.cancel()
    instances.forEach {
      it.terminate()
      it.close()
    }
  }
}
