package com.hlyue.teamcity.agent.netease

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hlyue.teamcity.agent.netease.api.DiskCxtResponse
import kotlinx.coroutines.experimental.*
import org.apache.commons.lang3.RandomStringUtils
import org.json.JSONObject
import java.io.Closeable
import java.util.concurrent.TimeUnit

class NeteaseDiskProvider(private val profileId: String,
                          private val connector: NeteaseOpenApiConnector):Closeable {

  companion object {
    val DOCKER_DISK_PREFIX = "tc-agent-docker-"
    val DISK_TYPE = "CloudHighPerformanceSsd"
    val DISK_SNAPSHOT = 5235
  }
  private val constants = Constants()
  private val context = newSingleThreadContext("disk-$profileId")
  private val createdDisks = mutableListOf<Int>()
  private val logger = Constants.buildLogger()
  private val gson = Gson()

  private fun createDisk() = async(context) {
    try {
      val name = "$DOCKER_DISK_PREFIX${RandomStringUtils.randomAlphabetic(4).toLowerCase()}"
      val response = connector.NeteaseOpenApiRequestBuilder(
        serviceName = "ncv",
        action = "CreateDisk",
        version = "2017-12-28",
        query = mapOf(
          "PricingModel" to "PostPaid",
          "ZoneId" to constants.NETEASE_ZONE_ID,
          "Name" to name,
          "Type" to DISK_TYPE,
          "Scope" to "NCS",
          "Capacity" to 30.toString()
        )
      ).request().await()
      val json = JSONObject(response)
      val id = json.getJSONArray("DiskIds").first() as Int
      while (true) {
        delay(5, TimeUnit.SECONDS)
        val jsonObject = JSONObject(
          connector.NeteaseOpenApiRequestBuilder(
            serviceName = "ncv",
            action = "DescribeDisk",
            version = "2017-12-28",
            query = mapOf(
              "DiskId" to id.toString()
            )
          ).request().await()
        ).getJSONObject("DiskCxt")
        val status = jsonObject.getString("Status")
        if (status == "create_succ") break
      }
      id
    } catch (e: Exception) {
      logger.info("disk add failed", e)
      0
    }
  }

  private fun getAvailableDisk() = async(context) {
    try {
      val response = connector.NeteaseOpenApiRequestBuilder(
        serviceName = "ncv",
        action = "ListDisk",
        version = "2017-12-28",
        query = mapOf(
          "ZoneId" to constants.NETEASE_ZONE_ID
        )
      ).request().await()
      val json = JSONObject(response)
      json.getJSONArray("DiskCxts")
        .map { gson.fromJson(it.toString(), DiskCxtResponse::class.java) }
        .firstOrNull { isDiskAvailable(it) }?.DiskId
    } catch (e: Exception) {
      logger.info("List disk failed", e)
      null
    }
  }

  fun getDockerDisk() = async(context) {
    getAvailableDisk().await() ?: createDisk().await()
  }

  fun removeDisk(diskId: Int) = launch(context) {
    if (createdDisks.remove(diskId)) {
      for (i in 1..5) {
        delay(10, TimeUnit.SECONDS)
        connector.NeteaseOpenApiRequestBuilder(
          serviceName = "ncv",
          version = "2017-12-28",
          action = "DeleteDisk",
          query = mapOf(
            "DiskId" to diskId.toString()
          )
        ).request().await()
      }
    }
  }

  override fun close() {
    context.close()
  }

  private fun isDiskAvailable(disk: DiskCxtResponse) : Boolean {
    return disk.DiskName.startsWith(DOCKER_DISK_PREFIX) &&
      disk.ZoneId == constants.NETEASE_ZONE_ID &&
      disk.AttachedInstance.isBlank() &&
      disk.Type == DISK_TYPE
  }

}
