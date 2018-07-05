package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.clouds.*
import jetbrains.buildServer.serverSide.AgentDescription

class NeteaseCloudClient: CloudClientEx {
  override fun findInstanceByAgent(agent: AgentDescription): CloudInstance? {
    return null
  }

  override fun getErrorInfo(): CloudErrorInfo? {
    return null
  }

  override fun findImageById(imageId: String): CloudImage? {
    return null
  }

  override fun canStartNewInstance(image: CloudImage): Boolean {
    return true
  }

  override fun isInitialized(): Boolean {
    return true
  }

  override fun generateAgentName(agent: AgentDescription): String? {
    return "name"
  }

  override fun getImages(): Collection<out CloudImage> {
    return emptyList()
  }

  override fun startNewInstance(image: CloudImage, tag: CloudInstanceUserData): CloudInstance {
    return NeteaseCloudInstance()
  }

  override fun terminateInstance(instance: CloudInstance) {
  }

  override fun restartInstance(instance: CloudInstance) {
  }

  override fun dispose() {
  }
}
