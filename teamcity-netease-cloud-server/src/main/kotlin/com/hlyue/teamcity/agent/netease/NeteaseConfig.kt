package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.clouds.CloudClientParameters

class NeteaseConfig private constructor(

  val accessKey: String,

  val accessSecret: String,

  val serverUrl: String,

  val machineType: String,

  val vpcId: String,

  val subnetId: String,

  val securityGroupId: String,

  val namespaceId: Long

) {

  companion object {

    val constants = Constants()

    fun buildFromCloudConfig(cloudClientParameters: CloudClientParameters): NeteaseConfig {
      return NeteaseConfig(
        accessKey = cloudClientParameters.getParameter(constants.PREFERENCE_ACCESS_KEY)!!,
        accessSecret = cloudClientParameters.getParameter(constants.PREFERENCE_ACCESS_SECRET)!!,
        serverUrl = cloudClientParameters.getParameter(constants.PREFERENCE_SERVER_URL)!!,
        machineType = cloudClientParameters.getParameter(constants.PREFERENCE_MACHINE_TYPE)!!,
        vpcId = cloudClientParameters.getParameter(constants.PREFERENCE_VPC)!!,
        subnetId = cloudClientParameters.getParameter(constants.PREFERENCE_SUBNET)!!,
        securityGroupId = cloudClientParameters.getParameter(constants.PREFERENCE_SECURITY_GROUP)!!,
        namespaceId = cloudClientParameters.getParameter(constants.PREFERENCE_NAMESPACE)?.toLong() ?: 0L
      )
    }

  }

}
