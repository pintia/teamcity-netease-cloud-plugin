package com.hlyue.teamcity.agent.netease

import com.google.gson.JsonObject
import jetbrains.buildServer.clouds.*
import jetbrains.buildServer.clouds.InstanceStatus.*
import jetbrains.buildServer.serverSide.AgentDescription
import kotlinx.coroutines.experimental.*
import org.apache.commons.lang3.RandomStringUtils
import org.json.JSONObject
import java.time.Instant
import java.util.*

class NeteaseCloudInstance(val workloadName: String,
                           private val neteaseCloudImage: NeteaseCloudImage,
                           private val connector: NeteaseOpenApiConnector,
                           private val config: NeteaseConfig) : CloudInstance {

  companion object : Constants()

  val envWorkloadId = RandomStringUtils.randomAlphabetic(8).toLowerCase()

  var workloadId: Long = 0L

  private var lastError: CloudErrorInfo? = null
  private val now = Instant.now()
  private val context = newSingleThreadContext("instance:$envWorkloadId")

  fun getInfo(): Deferred<JSONObject?> = async(context) {
    lastError = null
    var response = "{}"
    try {
      response = connector.NeteaseOpenApiRequestBuilder(
        action = "DescribeStatefulWorkloadInfo",
        version = "2017-11-16",
        serviceName = "ncs",
        query = mapOf(
          "NamespaceId" to config.namespaceId.toString(),
          "StatefulWorkloadId" to workloadId.toString()
        )
      ).request().await()
      JSONObject(response)
    } catch (e: Exception) {
      lastError = CloudErrorInfo("GetInfo", "Json parse failed, res:$response", e)
      null
    }
  }

  override fun getStatus(): InstanceStatus = runBlocking(context) {
    lastError = null
    var response: JSONObject? = null
    try {
      response = getInfo().await()
      when (response!!.getString("StatefulWorkloadStatus")) {
      //https://www.163yun.com/help/documents/157254714362351616
        "Creating" -> STARTING
        "CreateFail" -> ERROR
        "Updating" -> STARTING
        "Running" -> RUNNING
        "Abnormal" -> ERROR
        else -> STOPPED
      }
    } catch (e: Exception) {
      lastError = CloudErrorInfo("getStatus", "failed, response: ${response?.toString()}", e)
      ERROR
    }
  }

  override fun getInstanceId(): String {
    return "workload:$workloadId"
  }

  override fun getName(): String = workloadName

  override fun getStartedTime(): Date {
    return Date.from(now)
  }

  override fun getImage(): CloudImage = neteaseCloudImage

  override fun getNetworkIdentity(): String? {
    return null
  }

  override fun getImageId(): String = neteaseCloudImage.id

  override fun getErrorInfo(): CloudErrorInfo? {
    return lastError
  }

  override fun containsAgent(agent: AgentDescription): Boolean {
    val workloadId = agent.configurationParameters[ENV_INSTANCE_ID]
    return envWorkloadId == workloadId
  }

  fun terminate() = runBlocking(context) {
    connector.NeteaseOpenApiRequestBuilder(
      action = "DeleteStatefulWorkload",
      version = "2017-11-16",
      serviceName = "ncs",
      query = mapOf(
        "NamespaceId" to config.namespaceId.toString(),
        "StatefulWorkloadId" to workloadId.toString()
      )
    )
  }

  fun forceRestart() = runBlocking(context) {
    connector.NeteaseOpenApiRequestBuilder(
      action = "RestartStatefulWorkloadInstance",
      version = "2017-11-16",
      serviceName = "ncs",
      query = mapOf(
        "NamespaceId" to config.namespaceId.toString(),
        "StatefulWorkloadId" to workloadId.toString()
      )
    ).request().await()
  }
}
