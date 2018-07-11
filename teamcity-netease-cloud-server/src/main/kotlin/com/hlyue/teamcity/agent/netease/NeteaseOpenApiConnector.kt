package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.python.util.PythonInterpreter
import org.springframework.core.io.ClassPathResource
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.time.Instant
import java.time.format.DateTimeFormatter

class NeteaseOpenApiConnector(private val accessKey: String,
                              private val accessSecret: String,
                              private val pluginDescriptor: PluginDescriptor) {

  companion object {
    val host = "open.c.163.com"
    val zone = "cn-east-1"
    val contentType = "application/json"
    val logger = Constants.buildLogger()
  }

  inner class NeteaseOpenApiRequestBuilder(
    val action: String,
    val version: String,
    val url: String,
    val serviceName: String = "ncs",
    val method: String = "GET",
    val data: String = ""
  ) {

    val date: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

    fun buildArgs(): String {
      val args = mutableListOf("./resources.py",
        "--region=$zone",
        "--service=$serviceName",
        "--access-key=$accessKey",
        "--access-secret=$accessSecret",
        "-X", method,
        "-H", "'Content-Type: $contentType'"
      )
      if (!data.isBlank()) {
        args.add(data)
      }
      args.add("https://$host/$serviceName?Action=$action&Version=$version")
      return args.joinToString(",") { "\"$it\""}
    }

    fun request(): String {
      val interpreter = PythonInterpreter()
      val out = ByteArrayOutputStream()
      val err = ByteArrayOutputStream()
      val args = buildArgs()
      logger.info("args: $args")
      interpreter.setOut(out)
      interpreter.setErr(err)
      interpreter.exec("""
          import sys
          sys.argv = [$args]
      """.trimIndent())
      try {
        interpreter.execfile(Resources.signaturePy.byteInputStream(), "resources.py")
      } catch (e: Throwable) {
        logger.error(e)
      }
      val content = out.toByteArray()!!.toString(Charsets.UTF_8)
      logger.info("response: $content")
      logger.info("error: ${err.toByteArray()!!.toString(Charsets.UTF_8)}")
      return content
    }
  }
}
