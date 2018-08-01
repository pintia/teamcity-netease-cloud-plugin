package com.hlyue.teamcity.agent.netease

import com.google.common.base.Strings
import jetbrains.buildServer.clouds.CloudClientParameters
import java.util.*

class NeteaseConfig private constructor(

  val accessKey: String,

  val accessSecret: String,

  val machineType: String,

  val vpcId: String,

  val subnetId: String,

  val securityGroupId: String,

  val namespaceId: Long,

  val imageTag: String

) {

  companion object {

    val constants = Constants()

    fun buildFromCloudConfig(cloudClientParameters: CloudClientParameters): NeteaseConfig {
      val accessKey = getParameter(cloudClientParameters, constants.PREFERENCE_ACCESS_KEY)
      val accessSecret = getParameter(cloudClientParameters, constants.PREFERENCE_ACCESS_SECRET)
      val machineType = getParameter(cloudClientParameters, constants.PREFERENCE_MACHINE_TYPE)
      val vpcId = getParameter(cloudClientParameters, constants.PREFERENCE_VPC)
      val subnetId = getParameter(cloudClientParameters, constants.PREFERENCE_SUBNET)
      val securityGroupId = getParameter(cloudClientParameters, constants.PREFERENCE_SECURITY_GROUP)
      val namespaceId = getParameter(cloudClientParameters, constants.PREFERENCE_NAMESPACE)
      val imageTag = getParameter(cloudClientParameters, constants.PREFERENCE_IMAGE_TAG)

      return NeteaseConfig(
        accessKey = accessKey,
        accessSecret = accessSecret,
        machineType = machineType,
        vpcId = vpcId,
        subnetId = subnetId,
        securityGroupId = securityGroupId,
        namespaceId = namespaceId.toLong(),
        imageTag = imageTag
      )
    }

    private fun getParameter(clientParameters: CloudClientParameters, key: String): String {
      val value = clientParameters.getParameter(key)
      if (value.isNullOrBlank()) throw ParameterNullException(key)
      return value!!
    }

    class ParameterNullException(name: String): NullPointerException("$name should not be null")

  }

}
