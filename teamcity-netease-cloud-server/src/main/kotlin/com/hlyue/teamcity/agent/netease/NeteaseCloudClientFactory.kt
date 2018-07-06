package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.clouds.*
import jetbrains.buildServer.controllers.BasePropertiesBean
import jetbrains.buildServer.serverSide.AgentDescription
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.ServerPaths
import jetbrains.buildServer.serverSide.ServerSettings
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

  private val propertiesBean = BasePropertiesBean(null)

  init {
    cloudRegistrar.registerCloudFactory(this)
  }

  override fun getInitialParameterValues(): Map<String, String> {
    return mapOf(CREDENTIALS_TYPE to CREDENTIALS_ENVIRONMENT)
  }

  override fun canBeAgentOfType(description: AgentDescription): Boolean {
    return true
  }

  override fun getDisplayName(): String = DISPLAY_NAME

  override fun getEditProfileUrl(): String? {
    return pluginDescriptor.getPluginResourcesPath("settings.html")
  }

  override fun createNewClient(state: CloudState, params: CloudClientParameters): CloudClientEx {
    return NeteaseCloudClient(propertiesBean.properties[PREFERENCE_MACHINE_TYPE])
  }

  override fun getCloudCode(): String = CLOUD_CODE

  override fun getPropertiesProcessor(): PropertiesProcessor {
    return PropertiesProcessor {
      emptyList()
    }
  }
}
