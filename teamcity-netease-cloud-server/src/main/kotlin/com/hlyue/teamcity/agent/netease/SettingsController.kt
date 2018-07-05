package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SettingsController(private val server: SBuildServer,
                         private val pluginDescriptor: PluginDescriptor,
                         private val manager: WebControllerManager): BaseController(server) {

  private val myJspPath: String = pluginDescriptor.getPluginResourcesPath("settings.jsp")
  private val myHtmlPath: String = pluginDescriptor.getPluginResourcesPath("settings.html")

  override fun doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView? {
    request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true)
    return if (isPost(request)) {
      // TODO
      null
    } else {
      doGet(request, response)
    }
  }

  fun doGet(request: HttpServletRequest, response: HttpServletResponse): ModelAndView {
    return ModelAndView(myJspPath)
  }

  init {
    manager.registerController(myHtmlPath, this)
  }
}
