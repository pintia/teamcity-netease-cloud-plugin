package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.EventDispatcher

class EnvProvider(agentEvents: EventDispatcher<AgentLifeCycleListener>,
                  private val agentConfigurationEx: BuildAgentConfigurationEx) {
  init {
    agentEvents.addListener(object : AgentLifeCycleAdapter() {
      override fun afterAgentConfigurationLoaded(agent: BuildAgent) {
        super.afterAgentConfigurationLoaded(agent)
        appendEcsSpecificConfiguration()
      }
    })
  }

  companion object: Constants()

  private fun appendEcsSpecificConfiguration() {
    val environment = System.getenv()
    val tcAgentId = environment[ENV_NETEASE_TC_AGENT] ?: ""
    val instanceId = environment[ENV_INSTANCE_ID] ?: ""
    val providedServerUrl = environment[ENV_SERVER_URL] ?: ""
    val name = environment[ENV_INSTANCE_NAME] ?: ""
    agentConfigurationEx.serverUrl = providedServerUrl
    agentConfigurationEx.addConfigurationParameter(ENV_NETEASE_TC_AGENT, tcAgentId)
    agentConfigurationEx.addConfigurationParameter(ENV_INSTANCE_ID, instanceId)
    agentConfigurationEx.addConfigurationParameter(ENV_INSTANCE_NAME, name)
    agentConfigurationEx.name = name
  }
}
