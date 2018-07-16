package com.hlyue.teamcity.agent.netease

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import jetbrains.buildServer.web.openapi.PluginDescriptor
import kotlinx.coroutines.experimental.*
import org.python.antlr.ast.Str
import org.python.util.PythonInterpreter
import org.springframework.core.io.ClassPathResource
import java.io.*
import java.time.Instant
import java.time.format.DateTimeFormatter

class NeteaseOpenApiConnector(private val accessKey: String,
                              private val accessSecret: String) {

  companion object {
    val host = "open.c.163.com"
    val zone = "cn-east-1"
    val contentType = "application/json"
    val logger = Constants.buildLogger()
    val gson = Gson()
    val context = newSingleThreadContext("connector")
  }

  inner class NeteaseOpenApiRequestBuilder(
    val action: String,
    val version: String,
    val serviceName: String = "ncs",
    val method: String = "GET",
    val data: String = "{}",
    val query: Map<String, String> = emptyMap()
  ) {

    private fun buildArgs(): String {
      val args = mutableListOf("./resources.py",
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
      return args.joinToString(",") { "'$it'"}
    }

    fun request(): Deferred<String> = async(context) {
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
      val type = object : TypeToken<List<String>>() { }.type
      val reader = JsonReader(StringReader(content))
      reader.isLenient = true
      val list = gson.fromJson<List<String>>(reader, type)
      val (stdout, stderr) = runCommand(list.map {
        it.trim()
      }.toTypedArray())
      logger.info("curl out: $stdout")
      interpreter.cleanup()
      interpreter.close()
      stdout
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
