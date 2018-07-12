package com.hlyue.teamcity.agent.netease

import com.google.gson.Gson
import jetbrains.buildServer.clouds.*

class NeteaseCloudImage(private val specType: String): CloudImage {

  private val logger = Constants.buildLogger()
  private val gson = Gson()

  val instances = mutableListOf<NeteaseCloudInstance>()

  override fun getAgentPoolId(): Int? = null

  override fun getName(): String = specType

  override fun getId(): String = specType

  override fun getInstances(): Collection<out CloudInstance> {
    logger.info("getInstances: ${gson.toJson(instances)}")
    return instances
  }

  override fun getErrorInfo(): CloudErrorInfo? {
    return null
  }

  override fun findInstanceById(id: String): CloudInstance? {
    logger.info("findInstanceById: $id, myInstances: ${gson.toJson(instances)}")
    return instances.firstOrNull { it.instanceId == id }
  }
}
