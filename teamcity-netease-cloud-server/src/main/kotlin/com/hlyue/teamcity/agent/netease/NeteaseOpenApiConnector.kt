package com.hlyue.teamcity.agent.netease

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.hlyue.teamcity.agent.netease.other.PythonRunner
import kotlinx.coroutines.async
import java.io.StringReader

class NeteaseOpenApiConnector(
  private val accessKey: String,
  private val accessSecret: String
) {

  companion object {
    val host = "open.c.163.com"
    val zone = "cn-east-1"
    val contentType = "application/json"
    val logger = Constants.buildLogger()
    val gson = Gson()
  }

  inner class NeteaseOpenApiRequestBuilder(
    val action: String,
    val version: String,
    val serviceName: String = "ncs",
    val method: String = "GET",
    val data: String = "{}",
    val query: Map<String, String> = emptyMap()
  ) {

    private fun buildArgs(): List<String> {
      val args = mutableListOf(
        "./resources.py",
        "--region=$zone",
        "--service=$serviceName",
        "--access-key=$accessKey",
        "--access-secret=$accessSecret",
        "-X", method,
        "-H", "content-type: $contentType"
      )
      if (method == "POST") {
        args.add("--data")
        args.add(data)
      }
      val additionQuery = query.entries.joinToString("&") { "${it.key}=${it.value}" }.let {
        if (it.isEmpty()) it else "&$it"
      }
      args.add("https://$host/$serviceName?Action=$action&Version=$version$additionQuery")
      return args
    }

    fun request(): String {
      val content = PythonRunner.runPythonScript(Resources.signaturePy, buildArgs()).first
      val type = object : TypeToken<List<String>>() {}.type
      val reader = JsonReader(StringReader(content))
      reader.isLenient = true
      val list = gson.fromJson<List<String>>(reader, type)
      val (stdout, _) = runCommand(list.map { it.trim() }.toTypedArray())
      return stdout
    }

    private fun runCommand(args: Array<String>): Pair<String, String> {
      logger.info("run command: ${args.joinToString(" ")}")
      val (output, error) = arrayOf(createTempFile(), createTempFile())
      output.deleteOnExit()
      error.deleteOnExit()

      val builder = ProcessBuilder(*args)
      builder.redirectOutput(output)
      builder.redirectError(error)
      val p = builder.start()
      p.waitFor()
      p.destroy()

      val outputContent = output.readText()
      val errorContent = error.readText()
      output.delete()
      error.delete()

      return outputContent to errorContent
    }
  }
}
