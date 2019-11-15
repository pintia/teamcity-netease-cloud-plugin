package com.hlyue.teamcity.agent.netease.other

import com.hlyue.teamcity.agent.netease.NeteaseOpenApiConnector
import kotlinx.coroutines.*
import org.python.util.PythonInterpreter
import java.io.ByteArrayOutputStream

class PythonRunner {

  companion object {

    val context = newSingleThreadContext("PythonRunner")

    fun runPythonScript(
      script: String,
      args: List<String> = emptyList(),
      fileName: String = "request.py"
    ) = GlobalScope.async(context) {
      // Cause of the limitation of jpython, running scripts in parallel is not working.
      // We are using kotlin co-routine to simply submit tasks to single thread.

      val interpreter = PythonInterpreter()
      val out = ByteArrayOutputStream()
      val err = ByteArrayOutputStream()
      interpreter.setOut(out)
      interpreter.setErr(err)
      try {
        if (args.isNotEmpty()) {
          interpreter.exec(
            """
            import sys
            sys.argv = [${args.joinToString(",") { "'${it.replace("\'", "\\'")}'" }}]
            """.trimIndent()
          )
        }
        interpreter.execfile(script.byteInputStream(), fileName)
      } catch (e: Throwable) {
        NeteaseOpenApiConnector.logger.error(e)
      } finally {
        interpreter.cleanup()
        interpreter.close()
      }
      listOf(out, err).map { it.toByteArray().toString(Charsets.UTF_8) }.let { it[0] to it[1] }
    }

  }
}
