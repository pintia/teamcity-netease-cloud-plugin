package com.hlyue.teamcity.agent.netease

import com.google.gson.Gson
import com.hlyue.teamcity.agent.netease.api.DiskCxtResponse
import kotlinx.coroutines.*
import org.apache.commons.lang3.RandomStringUtils
import org.json.JSONObject
import java.io.Closeable
import java.util.concurrent.TimeUnit

class NeteaseDiskProvider(private val profileId: String,
                          private val connector: NeteaseOpenApiConnector) {

  companion object {
    val DOCKER_DISK_PREFIX = "tc-agent-docker-"
    val DISK_TYPE = "CloudSsd"
    val LIST_DISK_LIMIT = "10000"
  }
  private val constants = Constants()
  private val logger = Constants.buildLogger()
  private val gson = Gson()

  private suspend fun createDisk(prefix: String, config: NeteaseConfig): Int {
    return try {
      val name = "$prefix${RandomStringUtils.randomAlphabetic(4).toLowerCase()}"
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
          "Capacity" to config.diskSize.toString()
        )
      ).request()
      val json = JSONObject(response)
      val id = json.getJSONArray("DiskIds").first() as Int
      while (true) {
        delay(5 * 1000)
        val jsonObject = JSONObject(
          connector.NeteaseOpenApiRequestBuilder(
            serviceName = "ncv",
            action = "DescribeDisk",
            version = "2017-12-28",
            query = mapOf(
              "DiskId" to id.toString()
            )
          ).request()
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

  private suspend fun getAvailableDisk(prefix: String): Int? {
    return try {
      val response = connector.NeteaseOpenApiRequestBuilder(
        serviceName = "ncv",
        action = "ListDisk",
        version = "2017-12-28",
        query = mapOf(
          "ZoneId" to constants.NETEASE_ZONE_ID,
          "Limit" to LIST_DISK_LIMIT
        )
      ).request()
      val json = JSONObject(response)
      json.getJSONArray("DiskCxts")
        .map { gson.fromJson(it.toString(), DiskCxtResponse::class.java) }
        .firstOrNull { isDiskAvailable(it, prefix) }?.DiskId
    } catch (e: Exception) {
      logger.info("List disk failed", e)
      null
    }
  }

  suspend fun getDockerDisk(config: NeteaseConfig): Int {
    return getAvailableDisk(DOCKER_DISK_PREFIX) ?: createDisk(DOCKER_DISK_PREFIX, config)
  }

  private fun isDiskAvailable(disk: DiskCxtResponse, prefix: String) : Boolean {
    return disk.DiskName.startsWith(prefix) &&
      disk.ZoneId == constants.NETEASE_ZONE_ID &&
      disk.AttachedInstance.isBlank() &&
      disk.Type == DISK_TYPE
  }

}
