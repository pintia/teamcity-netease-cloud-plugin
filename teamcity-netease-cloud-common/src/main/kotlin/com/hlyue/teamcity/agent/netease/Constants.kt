package com.hlyue.teamcity.agent.netease

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.log.Loggers

open class Constants {

  val CLOUD_CODE = "163"

  val DISPLAY_NAME = "Netease Cloud"

  val CREDENTIALS_TYPE = "credentialsType"

  val CREDENTIALS_ENVIRONMENT = "environment"

  val PREFERENCE_ACCESS_KEY = "accessKey"

  val PREFERENCE_ACCESS_SECRET = "accessSecret"

  val PREFERENCE_MACHINE_TYPE = "machineType"

  val PREFERENCE_NAMESPACE = "namespace"

  val PREFERENCE_VPC = "vpc"

  val PREFERENCE_SUBNET = "subnet"

  val PREFERENCE_SECURITY_GROUP = "securityGroup"

  val MACHINE_TYPE_LIST = listOf("ncs.n1.small2", "ncs.n1.medium4", "ncs.n1.large8")

  companion object {
    fun buildLogger() = Loggers.ACTIVITIES
  }


}
