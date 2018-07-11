package com.hlyue.teamcity.agent.netease.web

import com.google.gson.Gson
import com.hlyue.teamcity.agent.netease.Constants
import com.hlyue.teamcity.agent.netease.Constants.Companion.buildLogger
import com.hlyue.teamcity.agent.netease.NeteaseOpenApiConnector
import jetbrains.buildServer.controllers.*
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

  private companion object : Constants()

  private val myJspPath: String = pluginDescriptor.getPluginResourcesPath("settings.jsp")
  private val myHtmlPath: String = pluginDescriptor.getPluginResourcesPath("settings.html")
  private val gson = Gson()

  val myLogger = buildLogger()

  override fun doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView? {
    return if (isPost(request)) {
      response.contentType = "application/json"
      response.outputStream.write(doPost(request, response).toByteArray())
      null
    } else {
      doGet(request, response)
    }
  }

  fun doGet(request: HttpServletRequest, response: HttpServletResponse): ModelAndView {
    val mv = ModelAndView(myJspPath)
    mv.model["basePath"] = myHtmlPath
    return mv
  }

  fun doPost(request: HttpServletRequest, response: HttpServletResponse): String {
    val post = request.inputStream.readBytes().toString(Charsets.UTF_8)
    myLogger.info("post content: $post")

    val param = gson.fromJson(post, PostRequest::class.java)
    val response =
      NeteaseOpenApiConnector(param.accessKey, param.accessSecret, pluginDescriptor).NeteaseOpenApiRequestBuilder(
        action = "DescribeNamespaces",
        url = "ncs",
        serviceName = "ncs",
        version = "2017-11-16"
      ).request()
    return response
  }

  init {
    manager.registerController(myHtmlPath, this)
  }
}
