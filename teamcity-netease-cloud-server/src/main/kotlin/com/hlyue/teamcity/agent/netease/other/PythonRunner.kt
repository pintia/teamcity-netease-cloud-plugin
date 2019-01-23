package com.hlyue.teamcity.agent.netease.other

import com.hlyue.teamcity.agent.netease.NeteaseOpenApiConnector
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.python.util.PythonInterpreter
import java.io.ByteArrayOutputStream

class PythonRunner {

  companion object {

    fun runPythonScript(
      script: String,
      args: List<String> = emptyList(),
      fileName: String = "request.py"
    ): Pair<String, String> {
      // Cause of the limitation of jpython, running scripts in parallel is not working.
      // We are using kotlin co-routine to simply submit tasks to single thread.

      val interpreter = PythonInterpreter()
      val out = ByteArrayOutputStream()
      val err = ByteArrayOutputStream()
      interpreter.setOut(out)
      interpreter.setErr(err)

      if (args.isNotEmpty()) {
        interpreter.exec(
          """
          import sys
          sys.argv = [${args.joinToString(",") { "'${it.replace("\'", "\\'")}'" }}]
      """.trimIndent()
        )
      }
      try {
        interpreter.execfile(script.byteInputStream(), fileName)
      } catch (e: Throwable) {
        NeteaseOpenApiConnector.logger.error(e)
      }
      interpreter.cleanup()
      interpreter.close()
      return listOf(out, err).map { it.toByteArray().toString(Charsets.UTF_8) }.let { it[0] to it[1] }
    }

  }
}
