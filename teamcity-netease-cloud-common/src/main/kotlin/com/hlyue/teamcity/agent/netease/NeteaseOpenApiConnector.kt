package com.hlyue.teamcity.agent.netease

import java.net.URLEncoder
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class NeteaseOpenApiConnector(private val accessKey: String,
                              private val accessSecret: String) {

  protected val host = "open.c.163.com"

  fun newRequest() {

  }

  inner class NeteaseOpenApiRequestBuilder(
    val action: String,
    val version: String,
    val url: String,
    val serviceName: String = "ncs",
    val method: String = "GET"
  ) {

    val date: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

    val queries = mutableMapOf<String, String>()

    init {
      queries["Action"] = action
      queries["Version"] = version
    }

    val credential: String
      get() {
        val date = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.CHINA).format(Instant.now())
        return "$accessKey/$date/cn-east-1/$serviceName/163_request"
      }


    val canonicalQueryString: String
      get() {
        fun encode(s: String) = URLEncoder.encode(s, "UTF-8")
        return queries.map { encode(it.key) to encode(it.value) }
          .toList()
          .sortedBy { it.first }
          .joinToString("&") { "${it.first}=${it.second}" }
      }

    val canonicalHeaders: String
      get() {
        mapOf(
          "host" to host,
          "x-163-date" to date,
        )
      }

    val canonicalRequest: String
      get() {
        "$method\n$/$serviceName\n"
      }

  }

}
