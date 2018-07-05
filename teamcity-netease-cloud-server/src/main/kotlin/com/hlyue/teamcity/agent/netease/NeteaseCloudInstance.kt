package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.clouds.CloudErrorInfo
import jetbrains.buildServer.clouds.CloudImage
import jetbrains.buildServer.clouds.CloudInstance
import jetbrains.buildServer.clouds.InstanceStatus
import jetbrains.buildServer.serverSide.AgentDescription
import java.util.*

class NeteaseCloudInstance: CloudInstance {
  override fun getStatus(): InstanceStatus {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getInstanceId(): String {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getName(): String {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getStartedTime(): Date {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getImage(): CloudImage {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getNetworkIdentity(): String? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getImageId(): String {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getErrorInfo(): CloudErrorInfo? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun containsAgent(agent: AgentDescription): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
