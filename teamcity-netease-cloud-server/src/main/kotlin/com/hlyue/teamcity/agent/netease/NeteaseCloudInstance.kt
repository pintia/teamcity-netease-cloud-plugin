package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.clouds.CloudErrorInfo
import jetbrains.buildServer.clouds.CloudImage
import jetbrains.buildServer.clouds.CloudInstance
import jetbrains.buildServer.clouds.InstanceStatus
import jetbrains.buildServer.clouds.InstanceStatus.RUNNING
import jetbrains.buildServer.clouds.InstanceStatus.STARTING
import jetbrains.buildServer.serverSide.AgentDescription
import java.time.Instant
import java.util.*

class NeteaseCloudInstance(val workloadId: Long,
                           val workloadName: String,
                           private val neteaseCloudImage: NeteaseCloudImage): CloudInstance {

  private val now = Instant.now()

  override fun getStatus(): InstanceStatus {
    return STARTING
  }

  override fun getInstanceId(): String {
    return "workload:$workloadId"
  }

  override fun getName(): String = workloadName

  override fun getStartedTime(): Date {
    return Date.from(now)
  }

  override fun getImage(): CloudImage = neteaseCloudImage

  override fun getNetworkIdentity(): String? {
    return null
  }

  override fun getImageId(): String = neteaseCloudImage.id

  override fun getErrorInfo(): CloudErrorInfo? {
    return null
  }

  override fun containsAgent(agent: AgentDescription): Boolean {
    return true
  }
}
