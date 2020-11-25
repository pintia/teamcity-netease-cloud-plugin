package com.hlyue.teamcity.agent.netease

import com.github.salomonbrys.kotson.jsonObject
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.log.Loggers

open class Constants {

  val NETEASE_ZONE_ID = "cn-east-1b"

  val CLOUD_CODE = "163"

  val DISPLAY_NAME = "Netease Cloud"

  val CREDENTIALS_TYPE = "credentialsType"

  val CREDENTIALS_ENVIRONMENT = "environment"

  val PREFERENCE_ACCESS_KEY = "accessKey"

  val PREFERENCE_ACCESS_SECRET = "accessSecret"

  val PREFERENCE_REPOSITORY_ID = "repositoryId"

  val PREFERENCE_IMAGE_TAG = "imageTag"

  val PREFERENCE_MACHINE_TYPE = "machineType"

  val PREFERENCE_NAMESPACE = "namespace"

  val PREFERENCE_VPC = "vpc"

  val PREFERENCE_SUBNET = "subnet"

  val PREFERENCE_SECURITY_GROUP = "securityGroup"

  val PREFERENCE_CREATE_DISK = "createDisk"

  val PREFERENCE_DISK_SIZE = "diskSize"

  val REQUIREMENTS = mapOf(
    "ncs.n1.small2" to jsonObject(
      "Cpu" to "1000",
      "Memory" to "2048"
    ),
    "ncs.n1.medium4" to jsonObject(
      "Cpu" to "2000",
      "Memory" to "4096"
    ),
    "ncs.n1.medium8" to jsonObject(
      "Cpu" to "2000",
      "Memory" to "8192"
    ),
    "ncs.n1.large8" to jsonObject(
      "Cpu" to "4000",
      "Memory" to "8192"
    ),
    "ncs.n1.large16" to jsonObject(
      "Cpu" to "4000",
      "Memory" to "16384"
    )
  )

  val MACHINE_TYPE_LIST = REQUIREMENTS.keys.toList()

  val ENV_INSTANCE_ID = "INSTANCE_ID"

  val ENV_NETEASE_TC_AGENT = "NETEASE_TC_AGENT"

  val ENV_INSTANCE_NAME = "INSTANCE_NAME"

  val ENV_SERVER_URL = "SERVER_URL"

  val ENV_YARN_CACHE_FOLDER = "YARN_CACHE_FOLDER"

  val ENV_YARN_CACHE_FOLDER_VALUE = "/var/lib/docker/yarn/"

  val ENV_GRADLE_USER_HOME = "GRADLE_USER_HOME"

  val ENV_GRADLE_USER_HOME_VALUE = "/var/lib/docker/gradle/"

  val ENV_AGENT_OPTS = "AGENT_OPTS"

  val ENV_AGENT_OPTS_VALUE = "workDir=/var/lib/docker/tc/work/" +
//    " tempDir=/var/lib/docker/tc/temp/" +
    " toolsDir=/var/lib/docker/tc/tools/" +
    " pluginsDir=/var/lib/docker/tc/plugins/" +
    " systemDir=/var/lib/docker/tc/system/"

  companion object {
    fun buildLogger() = Logger.getInstance(Loggers.CLOUD_CATEGORY_ROOT + "NETEASE")
  }


}
