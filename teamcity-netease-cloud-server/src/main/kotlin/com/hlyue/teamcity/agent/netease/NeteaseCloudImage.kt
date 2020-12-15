package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.clouds.*
import org.apache.commons.lang3.RandomStringUtils

class NeteaseCloudImage(private val specType: String,
                        private val cloudClient: NeteaseCloudClient): CloudImage {

  private val id = RandomStringUtils.randomAlphabetic(6).toLowerCase()

  private val logger = Constants.buildLogger()

  val instances = mutableListOf<NeteaseCloudInstance>()

  override fun getAgentPoolId(): Int? = cloudClient.agentPoolId

  override fun getName(): String = specType

  override fun getId(): String = id

  override fun getInstances(): Collection<CloudInstance> {
    return cloudClient.instances
  }

  override fun getErrorInfo(): CloudErrorInfo? {
    return null
  }

  override fun findInstanceById(id: String): CloudInstance? {
    return instances.firstOrNull { it.instanceId == id }
  }
}
