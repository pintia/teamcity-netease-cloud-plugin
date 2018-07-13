package com.hlyue.teamcity.agent.netease

import com.github.salomonbrys.kotson.jsonObject

class StatefulWorkloadCreateRequest {

  companion object {
    val REQUIREMENTS = mapOf(
      "ncs.n1.small2" to jsonObject(
        "Cpu" to "1000",
        "Memory" to "2048"
      ),
      "ncs.n1.medium4" to jsonObject(
        "Cpu" to "2000",
        "Memory" to "4096"
      ),
      "ncs.n1.large8" to jsonObject(
        "Cpu" to "4000",
        "Memory" to "8192"
      )
    )
  }

}
