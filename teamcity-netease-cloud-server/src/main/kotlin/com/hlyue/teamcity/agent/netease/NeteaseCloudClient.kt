package com.hlyue.teamcity.agent.netease

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jetbrains.buildServer.clouds.*
import jetbrains.buildServer.serverSide.AgentDescription
import jetbrains.buildServer.serverSide.ServerSettings
import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import java.math.BigInteger
import java.util.*

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
    logger.info("startNewInstance, image: ${image.id}, tag: $tag")

    try {
      val myImage = image as NeteaseCloudImage
      val name = "tc-${RandomStringUtils.randomAlphabetic(8).toLowerCase()}"

      val request = StatefulWorkloadCreateRequest(
        specType = cloudClientParameters.getParameter(PREFERENCE_MACHINE_TYPE)!!,
        VpcId = cloudClientParameters.getParameter(PREFERENCE_VPC)!!,
        SubnetId = cloudClientParameters.getParameter(PREFERENCE_SUBNET)!!,
        SecurityGroupId = cloudClientParameters.getParameter(PREFERENCE_SECURITY_GROUP)!!,
        namespaceId = cloudClientParameters.getParameter(PREFERENCE_NAMESPACE)?.toLong() ?: 0L,
        name = name,
        serverUrl = serverSettings.rootUrl
      )
      val response = connector.NeteaseOpenApiRequestBuilder(
        action = "CreateStatefulWorkload",
        method = "POST",
        url = "ncs",
        serviceName = "ncs",
        version = "2017-11-16",
        data = gson.toJson(request)
      ).request()
      logger.info("startNewInstance response: $response")
      val json = gson.fromJson(response.await(), StatefulWorkloadCreateResponse::class.java)

      val instance = NeteaseCloudInstance(json.StatefulWorkloadId, name, myImage)
      instances.add(instance)
      myImage.instances.add(instance)
      lastError = null
      instance
    } catch (e: Exception) {
      logger.infoAndDebugDetails("startNewInstance", e)
      lastError = CloudErrorInfo("cannot start new instant", e.localizedMessage, e)
      instances.first()
    }
  }

  override fun terminateInstance(instance: CloudInstance) {
  }

  override fun restartInstance(instance: CloudInstance) {
  }

  override fun dispose() {
    logger.info("dispose:")
  }
}
