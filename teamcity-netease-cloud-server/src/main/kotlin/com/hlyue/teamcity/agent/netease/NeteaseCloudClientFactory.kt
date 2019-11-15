package com.hlyue.teamcity.agent.netease

import com.hlyue.teamcity.agent.netease.Constants.Companion.buildLogger
import jetbrains.buildServer.clouds.*
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.springframework.stereotype.Component

@Component
class NeteaseCloudClientFactory(
  private val cloudRegistrar: CloudRegistrar,
  private val serverPaths: ServerPaths,
  private val pluginDescriptor: PluginDescriptor,
  private val serverSettings: ServerSettings
) : CloudClientFactory {

  companion object : Constants()

  private val logger = buildLogger()

  init {
    cloudRegistrar.registerCloudFactory(this)
  }

  override fun getInitialParameterValues(): Map<String, String> {
    return mapOf(CREDENTIALS_TYPE to CREDENTIALS_ENVIRONMENT)
  }

  override fun canBeAgentOfType(description: AgentDescription): Boolean {
    return description.configurationParameters.containsKey(ENV_NETEASE_TC_AGENT)
  }

  override fun getDisplayName(): String = DISPLAY_NAME

  override fun getEditProfileUrl(): String? {
    return pluginDescriptor.getPluginResourcesPath("settings.html")
  }

  override fun createNewClient(state: CloudState, params: CloudClientParameters): CloudClientEx {
    params.listParameterNames().forEach {
      logger.info("param $it: ${params.getParameter(it)}")
    }
    var serverUrl = params.getParameter("profileServerUrl")
    if (serverUrl.isNullOrEmpty()) serverUrl = serverSettings.rootUrl
    return NeteaseCloudClient(state.profileId, params, serverUrl)
  }

  override fun getCloudCode(): String = CLOUD_CODE

  override fun getPropertiesProcessor(): PropertiesProcessor {
    return PropertiesProcessor {
      emptyList()
    }
  }
}
