package com.hlyue.teamcity.agent.netease

import kotlinx.coroutines.experimental.*
import org.json.JSONObject
import java.io.Closeable
import java.util.*
import java.util.concurrent.TimeUnit

class NeteaseDiskProvider(private val profileId: String,
                          private val connector: NeteaseOpenApiConnector):Closeable {

  companion object {
    val DISK_PREFIX = "tc-agent-"
    val DISK_SNAPSHOT = 5235
  }
  private val context = newSingleThreadContext("disk-$profileId")
  private val createdDisks = mutableListOf<Int>()
  private val logger = Constants.buildLogger()

  private fun createDisk(name: String, snapshotId: String = "") = async(context) {
    try {
      val response = connector.NeteaseOpenApiRequestBuilder(
        serviceName = "ncv",
        action = "CreateDisk",
        version = "2017-12-28",
        query = mapOf(
          "PricingModel" to "PostPaid",
          "ZoneId" to "cn-east-1b",
          "Name" to name,
          "Type" to "CloudSsd",
          "Scope" to "NCS",
          "Capacity" to 20.toString()
        ).let {
          if (snapshotId.isNotBlank()) {
            it + ("SnapshotId" to snapshotId)
          } else {
            it
          }
        }
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

  fun getDisks(name: String) = async(context) {
    val dataDisk = createDisk(name, DISK_SNAPSHOT.toString())
    val dockerDisk = createDisk("docker-$name")
    createdDisks.addAll(listOf(dataDisk.await(), dockerDisk.await()))
    dataDisk.await() to dockerDisk.await()
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

}
