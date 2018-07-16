package com.hlyue.teamcity.agent.netease.web

import com.google.gson.Gson
import com.hlyue.teamcity.agent.netease.Constants
import com.hlyue.teamcity.agent.netease.Constants.Companion.buildLogger
import com.hlyue.teamcity.agent.netease.NeteaseOpenApiConnector
import jetbrains.buildServer.controllers.*
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import kotlinx.coroutines.experimental.*
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import javax.servlet.AsyncContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SettingsController(private val server: SBuildServer,
                         private val pluginDescriptor: PluginDescriptor,
                         private val manager: WebControllerManager): BaseController(server) {

  private companion object : Constants()

  private val myJspPath: String = pluginDescriptor.getPluginResourcesPath("settings.jsp")
  private val myHtmlPath: String = pluginDescriptor.getPluginResourcesPath("settings.html")
  private val coroutineContext = newSingleThreadContext("jython")
  private val gson = Gson()

  val myLogger = buildLogger()

  override fun doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView? {
    return if (isPost(request)) {
      doPost(request, response)
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

  fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
    request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true)

    val post = request.inputStream.readBytes().toString(Charsets.UTF_8)
    val param = gson.fromJson(post, PostRequest::class.java)
    myLogger.info("post content: ${gson.toJson(param)}")
    val context = request.startAsync(request, response)
    val connector = NeteaseOpenApiConnector(param.accessKey, param.accessSecret)
    request(param, connector, context)
  }

  fun request(param: PostRequest, connector: NeteaseOpenApiConnector, context: AsyncContext) = runBlocking {
    val res = when (param.resource) {
      "namespace" -> getNamespace(connector)
      "vpc" -> getVpc(connector)
      "subnet" -> getSubnet(connector, param.params["VpcId"] ?: "")
      "securityGroup" -> getSecurityGroup(connector, param.params["VpcId"] ?: "")
      else -> null
    }
    context.response.contentType = "application/json"
    context.response.outputStream.write(res?.await()?.toByteArray())
    context.complete()
  }

  fun getNamespace(connector: NeteaseOpenApiConnector): Deferred<String> {
    return connector.NeteaseOpenApiRequestBuilder(
      action = "DescribeNamespaces",
      serviceName = "ncs",
      version = "2017-11-16"
    ).request()
  }

  fun getVpc(connector: NeteaseOpenApiConnector): Deferred<String> {
    return connector.NeteaseOpenApiRequestBuilder(
      action = "ListVpc",
      serviceName = "vpc",
      version = "2017-11-30"
    ).request()
  }

  fun getSubnet(connector: NeteaseOpenApiConnector, vpcId: String): Deferred<String> {
    return connector.NeteaseOpenApiRequestBuilder(
      action = "ListSubnet",
      version = "2017-11-30",
      serviceName = "vpc",
      query = mapOf(
        "VpcId" to vpcId
      )
    ).request()
  }

  fun getSecurityGroup(connector: NeteaseOpenApiConnector, vpcId: String): Deferred<String> {
    return connector.NeteaseOpenApiRequestBuilder(
      action = "ListSecurityGroup",
      version = "2017-11-30",
      serviceName = "vpc",
      query = mapOf(
        "VpcId" to vpcId
      )
    ).request()
  }

  init {
    manager.registerController(myHtmlPath, this)
  }
}
