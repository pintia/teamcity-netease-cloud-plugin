package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.clouds.CloudClientParameters

class NeteaseConfig private constructor(

  val accessKey: String,

  val accessSecret: String,

  val machineType: String,

  val vpcId: String,

  val subnetId: String,

  val securityGroupId: String,

  val namespaceId: Long,

  val repositoryId: Long,

  val imageTag: String,

  val imageFullTag: String,

  val createDisk: Boolean,

  val diskSize: Int,

  val agentPoolId: Int

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
      val repositoryId = getParameter(cloudClientParameters, constants.PREFERENCE_REPOSITORY_ID)
      val imageTag = getParameter(cloudClientParameters, constants.PREFERENCE_IMAGE_TAG)
      val imageFullTag = getParameter(cloudClientParameters, constants.PREFERENCE_IMAGE_FULL_TAG)
      val createDisk = getParameter(cloudClientParameters, constants.PREFERENCE_CREATE_DISK)
      val diskSize = getParameter(cloudClientParameters, constants.PREFERENCE_DISK_SIZE)
      val agentPoolId = getParameter(cloudClientParameters, constants.PREFERENCE_AGENT_POOL)

      return NeteaseConfig(
        accessKey = accessKey,
        accessSecret = accessSecret,
        machineType = machineType,
        vpcId = vpcId,
        subnetId = subnetId,
        securityGroupId = securityGroupId,
        namespaceId = namespaceId.toLong(),
        repositoryId = repositoryId.toLong(),
        imageTag = imageTag,
        imageFullTag = imageFullTag,
        createDisk = createDisk.toInt() == 1,
        diskSize = diskSize.toInt(),
        agentPoolId = agentPoolId.toInt()
      )
    }

    private fun getParameter(clientParameters: CloudClientParameters, key: String): String {
      val value = clientParameters.getParameter(key)
      if (value.isNullOrBlank()) throw ParameterNullException(key)
      return value
    }

    class ParameterNullException(name: String): NullPointerException("$name should not be null")

  }

}
