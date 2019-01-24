package com.hlyue.teamcity.agent.netease

import jetbrains.buildServer.clouds.CloudErrorInfo
import jetbrains.buildServer.clouds.CloudImage
import jetbrains.buildServer.clouds.CloudInstance
import jetbrains.buildServer.clouds.InstanceStatus
import jetbrains.buildServer.clouds.InstanceStatus.*
import jetbrains.buildServer.serverSide.AgentDescription
import kotlinx.coroutines.*
import org.apache.commons.lang3.RandomStringUtils
import org.json.JSONObject
import java.io.Closeable
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
  var errorCount = 0

  private var lastError: CloudErrorInfo? = null
  private val now = Instant.now()

  @Volatile
  var mStatus: InstanceStatus = SCHEDULED_TO_START

  fun setStatus(status: String) {
    if (mStatus == STOPPING) return // stopping means instance was deleted from netease cloud, and will disappear soon.
    mStatus = when (status) {
    //https://www.163yun.com/help/documents/157254714362351616
      "Creating" -> STARTING
      "CreateFail" -> ERROR
      "Updating" -> STARTING
      "Running" -> RUNNING
      "Abnormal" -> ERROR
      else -> UNKNOWN
    }
    if (mStatus != RUNNING) ++errorCount
    else errorCount = 0
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

  suspend fun terminate() {
    this.mStatus = STOPPING
    connector.NeteaseOpenApiRequestBuilder(
      action = "DeleteStatefulWorkload",
      version = "2017-11-16",
      serviceName = "ncs",
      query = mapOf(
        "NamespaceId" to config.namespaceId.toString(),
        "StatefulWorkloadId" to workloadId.toString()
      )
    ).request()
  }

  suspend fun forceRestart() {
    connector.NeteaseOpenApiRequestBuilder(
      action = "RestartStatefulWorkloadInstance",
      version = "2017-11-16",
      serviceName = "ncs",
      query = mapOf(
        "NamespaceId" to config.namespaceId.toString(),
        "StatefulWorkloadId" to workloadId.toString()
      )
    ).request()
  }

}
