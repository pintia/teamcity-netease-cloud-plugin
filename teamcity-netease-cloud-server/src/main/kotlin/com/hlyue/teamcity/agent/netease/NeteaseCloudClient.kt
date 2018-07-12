package com.hlyue.teamcity.agent.netease

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jetbrains.buildServer.clouds.*
import jetbrains.buildServer.serverSide.AgentDescription
import kotlinx.coroutines.experimental.runBlocking
import java.util.*

class NeteaseCloudClient(private val cloudClientParameters: CloudClientParameters): CloudClientEx {

  companion object : Constants()

  private val instances = mutableListOf<NeteaseCloudInstance>()

  private val gson = Gson()
  private val image = NeteaseCloudImage(cloudClientParameters.getParameter(PREFERENCE_MACHINE_TYPE)!!)
  private val connector = NeteaseOpenApiConnector(cloudClientParameters.getParameter(PREFERENCE_ACCESS_KEY)!!,
    cloudClientParameters.getParameter(PREFERENCE_ACCESS_SECRET)!!)
  private val logger = Constants.buildLogger()

  override fun findInstanceByAgent(agent: AgentDescription): CloudInstance? {
    logger.info("findInstanceByAgent: ${gson.toJson(agent)}")
    return instances.firstOrNull()
  }

  override fun getErrorInfo(): CloudErrorInfo? {
    return null
  }

  override fun findImageById(imageId: String): CloudImage? {
    logger.info("findImageById: $imageId, myImageId:${image.id}")
    return if (imageId == image.id) image else null
  }

  override fun canStartNewInstance(image: CloudImage): Boolean {
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
    logger.info("startNewInstance, image: ${gson.toJson(image)}, tag: ${gson.toJson(tag)}")

    try {
      val myImage = image as NeteaseCloudImage

      val request = StatefulWorkloadCreateRequest(
        specType = cloudClientParameters.getParameter(PREFERENCE_MACHINE_TYPE)!!,
        VpcId = cloudClientParameters.getParameter(PREFERENCE_VPC)!!,
        SubnetId = cloudClientParameters.getParameter(PREFERENCE_SUBNET)!!,
        SecurityGroupId = cloudClientParameters.getParameter(PREFERENCE_SECURITY_GROUP)!!,
        namespaceId = cloudClientParameters.getParameter(PREFERENCE_NAMESPACE)?.toLong() ?: 0L,
        name = UUID.randomUUID().toString()
      )
      val response = connector.NeteaseOpenApiRequestBuilder(
        action = "CreateStatefulWorkload",
        url = "ncs",
        serviceName = "ncs",
        version = "2017-11-16",
        data = gson.toJson(request)
      ).request()
      val type = object : TypeToken<Map<String, Long>>() { }.type
      val json = gson.fromJson<Map<String, Long>>(response.await(), type)

      val instance = NeteaseCloudInstance(json["StatefulWorkloadId"]!!, myImage)
      instances.add(instance)
      myImage.instances.add(instance)
      instance
    } catch (e: Exception) {
      logger.infoAndDebugDetails("startNewInstance", e)
      null!!
    }
  }

  override fun terminateInstance(instance: CloudInstance) {
  }

  override fun restartInstance(instance: CloudInstance) {
  }

  override fun dispose() {
  }
}
