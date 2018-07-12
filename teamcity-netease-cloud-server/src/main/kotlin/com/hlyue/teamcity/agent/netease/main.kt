package com.hlyue.teamcity.agent.netease

fun main(argv: Array<String>) {
  val response =
    NeteaseOpenApiConnector("aaa", "bbb").NeteaseOpenApiRequestBuilder(
      action = "DescribeNamespaces",
      url = "ncs",
      serviceName = "ncs",
      version = "2017-11-16"
    ).request()
}
