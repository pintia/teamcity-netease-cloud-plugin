package com.hlyue.teamcity.agent.netease

class StatefulWorkloadCreateRequest(
  specType: String,
  VpcId: String,
  SubnetId: String,
  SecurityGroupId: String,
  namespaceId: Long,
  name: String,
  serverUrl: String
) {

  val Placement = PlacementType()

  val SpecType = specType

  val VirtualPrivateCloud = VirtualPrivateCloudType(VpcId, SubnetId)

  val SecurityGroupIds = arrayOf(SecurityGroupId)

  val Labels = mapOf(
    "agent" to "tc"
  )

  val ContainerType = "Standard"

  val NamespaceId = namespaceId

  val Name = name

  val Containers = arrayOf(ContainersType(name, specType, serverUrl))

  val SecurityContext = SecurityContextType()

  class ContainersType(name: String, specType: String, serverUrl: String) {
    val Name = name

    val Image = "hub.c.163.com/patest/teamcity-agent:2018.1"

    val LogDirs = arrayOf("/opt/buildagent/temp/", "/opt/buildagent/logs/")

    val Args = emptyArray<String>()

    val ResourceRequirements = ResourceRequirementsType(specType)

    val Envs = arrayOf(EnvType("SERVER_URL",serverUrl))
  }

  class PlacementType {
    val ZoneId = arrayOf("cn-east-1b")
  }

  class VirtualPrivateCloudType(
    val VpcId: String,
    val SubnetId: String
  )

  class SecurityContextType {
    val Privilege = true
  }

  class ResourceLimitType(val Cpu: String, val Memory: String)
  class ResourceRequirementsType(specType: String) {
    val Limits = REQUIREMENTS[specType]!!
    val Requests = REQUIREMENTS[specType]!!
  }

  class EnvType(val Name: String, val Value: String)

  companion object {
    val REQUIREMENTS = mapOf(
      "ncs.n1.small2" to ResourceLimitType("1000", "2048"),
      "ncs.n1.medium4" to ResourceLimitType("2000", "4096"),
      "ncs.n1.large8" to ResourceLimitType("4000", "8192")
    )
  }

}
