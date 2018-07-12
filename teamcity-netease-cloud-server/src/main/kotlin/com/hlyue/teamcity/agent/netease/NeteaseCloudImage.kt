package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.clouds.*
import java.util.*

class NeteaseCloudImage(private val specType: String): CloudImage {

  private val id = UUID.randomUUID().toString()

  private val logger = Constants.buildLogger()

  val instances = mutableListOf<NeteaseCloudInstance>()

  override fun getAgentPoolId(): Int? = null

  override fun getName(): String = specType

  override fun getId(): String = id

  override fun getInstances(): Collection<out CloudInstance> {
    logger.info("getInstances: myInstances: ${instances.joinToString(",") { it.instanceId }}")
    return instances
  }

  override fun getErrorInfo(): CloudErrorInfo? {
    return null
  }

  override fun findInstanceById(id: String): CloudInstance? {
    logger.info("findInstanceById: $id, myInstances: ${instances.joinToString(",") { it.instanceId }}")
    return instances.firstOrNull { it.instanceId == id }
  }
}
