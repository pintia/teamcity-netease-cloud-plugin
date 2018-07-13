package com.hlyue.teamcity.agent.netease

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import jetbrains.buildServer.clouds.*
import jetbrains.buildServer.serverSide.AgentDescription
import jetbrains.buildServer.serverSide.ServerSettings
import kotlinx.coroutines.experimental.*
import org.apache.commons.lang3.RandomStringUtils

class NeteaseCloudClient(private val cloudClientParameters: CloudClientParameters,
                         private val serverSettings: ServerSettings): CloudClientEx {

  companion object : Constants()

  private val instances = mutableListOf<NeteaseCloudInstance>()

  private val gson = Gson()
  private val image = NeteaseCloudImage(cloudClientParameters.getParameter(PREFERENCE_MACHINE_TYPE)!!)
  private val connector = NeteaseOpenApiConnector(cloudClientParameters.getParameter(PREFERENCE_ACCESS_KEY)!!,
    cloudClientParameters.getParameter(PREFERENCE_ACCESS_SECRET)!!)
  private val logger = Constants.buildLogger()

  private var lastError: CloudErrorInfo? = null

  override fun findInstanceByAgent(agent: AgentDescription): CloudInstance? {
    logger.info("findInstanceByAgent: agent")
    return instances.firstOrNull()
  }

  override fun getErrorInfo(): CloudErrorInfo? {
    return lastError
  }

  override fun findImageById(imageId: String): CloudImage? {
    logger.info("findImageById: $imageId, myImageId:${image.id}")
    return if (imageId == image.id) image else null
  }

  override fun canStartNewInstance(image: CloudImage): Boolean {
    logger.info("canStartNewInstance: $image.id, ${instances.isEmpty()}")
    return instances.isEmpty()
  }

  override fun isInitialized(): Boolean {
    return true
  }

  override fun generateAgentName(agent: AgentDescription): String? {
    return "name"
  }

  override fun getImages(): Collection<CloudImage> {
    return listOf(image)
  }

  override fun startNewInstance(image: CloudImage, tag: CloudInstanceUserData): NeteaseCloudInstance = runBlocking {
    lastError = null
    logger.info("startNewInstance, image: ${image.id}, tag: $tag")
    var response: String? = null

    try {
      val config = NeteaseConfig.buildFromCloudConfig(cloudClientParameters)
      val myImage = image as NeteaseCloudImage
      val name = "tc-${RandomStringUtils.randomAlphabetic(8).toLowerCase()}"
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
          "agent-id" to instance.envWorkloadId
        ),
        "ContainerType" to "Standard",
        "NamespaceId" to config.namespaceId,
        "Name" to name,
        "Containers" to jsonArray(jsonObject(
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
              "Value" to serverSettings.rootUrl
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
            )
          ),
          "SecurityContext" to jsonObject(
            "Privilege" to true
          )
        ))
      )
      response = connector.NeteaseOpenApiRequestBuilder(
          action = "CreateStatefulWorkload",
          method = "POST",
          serviceName = "ncs",
          version = "2017-11-16",
          data = request.toString()
      ).request().await()
      logger.info("startNewInstance response: $response")
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
    (instance as NeteaseCloudInstance).terminate()
  }

  override fun restartInstance(instance: CloudInstance) {
    (instance as NeteaseCloudInstance).forceRestart()
  }

  override fun dispose() = runBlocking {
    logger.info("dispose:")
    instances.forEach { it.terminate() }
  }
}
