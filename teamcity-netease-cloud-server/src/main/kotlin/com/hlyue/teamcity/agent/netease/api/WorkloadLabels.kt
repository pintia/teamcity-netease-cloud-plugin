package com.hlyue.teamcity.agent.netease.api

class WorkloadLabels {

  var zone: String = ""

  var name: String = ""

  var tcAgent: String = ""

  var agentId: String = ""

  var profileId: String = ""

  fun isAgent(profileId: String): Boolean {
    return zone == "cn-east-1b" && tcAgent == "netease" && this.profileId == profileId
  }

}
