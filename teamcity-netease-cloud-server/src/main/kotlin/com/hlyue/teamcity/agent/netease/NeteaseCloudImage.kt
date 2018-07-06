package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.clouds.*

class NeteaseCloudImage(private val specType: String): CloudImage {
  override fun getAgentPoolId(): Int? = null

  override fun getName(): String = specType

  override fun getId(): String = specType

  override fun getInstances(): Collection<out CloudInstance> {
    return emptyList()
  }

  override fun getErrorInfo(): CloudErrorInfo? {
    return null
  }

  override fun findInstanceById(id: String): CloudInstance? {
    return null
  }
}
