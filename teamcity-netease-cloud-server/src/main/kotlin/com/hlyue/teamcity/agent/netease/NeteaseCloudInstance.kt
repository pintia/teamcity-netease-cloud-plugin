package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.clouds.CloudErrorInfo
import jetbrains.buildServer.clouds.CloudImage
import jetbrains.buildServer.clouds.CloudInstance
import jetbrains.buildServer.clouds.InstanceStatus
import jetbrains.buildServer.clouds.InstanceStatus.*
import jetbrains.buildServer.serverSide.AgentDescription
import kotlinx.coroutines.experimental.*
import org.apache.commons.lang3.RandomStringUtils
import org.json.JSONObject
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

class NeteaseCloudInstance(
  val workloadName: String,
  private val neteaseCloudImage: NeteaseCloudImage,
  private val connector: NeteaseOpenApiConnector,
  private val config: NeteaseConfig
) : CloudInstance {

  companion object : Constants()

  val envWorkloadId = RandomStringUtils.randomAlphabetic(8).toLowerCase()

  var workloadId: Long = 0L

  private var lastError: CloudErrorInfo? = null
  private val now = Instant.now()
  private val context = newSingleThreadContext("instance:$envWorkloadId")
  @Volatile
  private var mStatus: InstanceStatus = SCHEDULED_TO_START

  init {
    launch(context) {
      while (true) {
        try {
          delay(10, TimeUnit.SECONDS)
          mStatus = fetchStatus()
        } catch (e: Exception) {
        }
      }
    }
  }

  private suspend fun getInfo(): JSONObject? {
    lastError = null
    var response = "{}"
    return try {
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

  private suspend fun fetchStatus(): InstanceStatus {
    lastError = null
    var response: JSONObject? = null
    return try {
      response = getInfo()
      when (response!!.getString("Status")) {
      //https://www.163yun.com/help/documents/157254714362351616
        "Creating" -> STARTING
        "CreateFail" -> ERROR
        "Updating" -> STARTING
        "Running" -> RUNNING
        "Abnormal" -> ERROR
        else -> UNKNOWN
      }
    } catch (e: Exception) {
      lastError = CloudErrorInfo("getStatus", "failed, response: ${response?.toString()}", e)
      UNKNOWN
    }
  }

  override fun getStatus(): InstanceStatus = mStatus

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
    ).request().await()
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
