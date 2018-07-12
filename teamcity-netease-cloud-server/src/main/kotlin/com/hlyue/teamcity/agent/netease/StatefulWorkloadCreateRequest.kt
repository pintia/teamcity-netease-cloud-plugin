package com.hlyue.teamcity.agent.netease

class StatefulWorkloadCreateRequest(specType: String,
                                    VpcId: String,
                                    SubnetId: String,
                                    SecurityGroupId: String,
                                    namespaceId: Long,
                                    name: String
                                    ) {

  val Placement = object {
    val ZoneId = arrayOf("cn-east-1b")
  }

  val SpecType = specType

  val VirtualPrivateCloud = object {
    val VpcId = VpcId
    val SubnetId = SubnetId
  }

  val SecurityGroupIds = arrayOf(SecurityGroupId)

  val Labels = mapOf(
    "agent" to "tc"
  )

  val ContainerType = "standard"

  val NamespaceId = namespaceId

  val Name = name

  val container = object {

    val Name = name

    val Image = "hub.c.163.com/patest/teamcity-agent:2018.1"

    val LogDirs = arrayOf("/opt/buildagent/temp")

    val Args = emptyArray<String>()

    val ResourceRequirements = object {
      val Limits = object {
        val Cpu = "8000"
        val Memory = "8192"
      }
      val Requests = object {
        val Cpu = "8000"
        val Memory = "8192"
      }
    }

  }

  val Containers = arrayOf(container)

  val SecurityContext = object {
    val Privilege = true
  }

}
