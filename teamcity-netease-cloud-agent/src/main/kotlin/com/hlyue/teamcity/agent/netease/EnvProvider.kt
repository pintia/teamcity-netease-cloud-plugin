package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.messages.DefaultMessagesInfo
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.util.EventDispatcher
import java.io.File

class EnvProvider(agentEvents: EventDispatcher<AgentLifeCycleListener>,
                  private val agentConfigurationEx: BuildAgentConfigurationEx) {
  init {
    agentEvents.addListener(object : AgentLifeCycleAdapter() {

      override fun afterAgentConfigurationLoaded(agent: BuildAgent) {
        super.afterAgentConfigurationLoaded(agent)
        appendConfiguration()
      }

      override fun buildStarted(runningBuild: AgentRunningBuild) {
        super.buildStarted(runningBuild)
        val logger = runningBuild.buildLogger
        // TODO: currently netease high performance container will set dns search domain.
        logger.logMessage(DefaultMessagesInfo.createTextMessage("ready to fix netease dns", Status.NORMAL))
        try {
          fixNeteaseDns(logger)
          logger.logMessage(DefaultMessagesInfo.createTextMessage("fix netease dns success"))
        } catch (e: Exception) {
          logger.logMessage(DefaultMessagesInfo.createError(e))
        }
      }
    })
  }

  companion object: Constants()

  private fun appendConfiguration() {
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

  private fun fixNeteaseDns(logger: BuildProgressLogger) {
    val file = File("/etc/resolv.conf")
    logger.logMessage(DefaultMessagesInfo.createTextMessage("Current resolve file: ${file.readText()}"))
    file.writeText("nameserver 114.114.114.114")
  }
}
