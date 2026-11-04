package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GoldRates(
  val price24kPerGram: Double,
  val price22kPerGram: Double,
  val price21kPerGram: Double,
  val price18kPerGram: Double,
  val currency: String = "SAR",
  val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
  val isLive: Boolean = true,
  val marketSource: String = "Saudi Gold Market (سوق الذهب السعودي)"
)

class GoldPriceService {
  private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .build()

  suspend fun fetchLiveGoldRates(): GoldRates = withContext(Dispatchers.IO) {
    try {
      // 1. Fetch real-time gold spot price from live open gold API
      var ounceUsd = 2745.0
      var fetchedLive = false

      try {
        val goldReq = Request.Builder()
          .url("https://api.gold-api.com/price/XAU")
          .header("User-Agent", "MasareefApp/1.0")
          .build()
        val goldResp = client.newCall(goldReq).execute()
        if (goldResp.isSuccessful) {
          val jsonStr = goldResp.body?.string()
          if (!jsonStr.isNullOrBlank()) {
            val json = JSONObject(jsonStr)
            if (json.has("price")) {
              val p = json.getDouble("price")
              if (p > 1000.0) {
                ounceUsd = p
                fetchedLive = true
              }
            }
          }
        }
      } catch (_: Exception) {
        // Fallback to secondary market calculation
      }

      // Fixed official peg in Saudi Arabia
      val usdToSar = 3.7515
      val gramsPerOunce = 31.1034768

      // Exact per-gram computation in SAR
      val gram24kSar = (ounceUsd / gramsPerOunce) * usdToSar

      val p24 = Math.round(gram24kSar * 100.0) / 100.0
      val p22 = Math.round((p24 * (22.0 / 24.0)) * 100.0) / 100.0
      val p21 = Math.round((p24 * (21.0 / 24.0)) * 100.0) / 100.0
      val p18 = Math.round((p24 * (18.0 / 24.0)) * 100.0) / 100.0

      GoldRates(
        price24kPerGram = p24,
        price22kPerGram = p22,
        price21kPerGram = p21,
        price18kPerGram = p18,
        currency = "SAR",
        lastUpdatedTimestamp = System.currentTimeMillis(),
        isLive = fetchedLive,
        marketSource = if (fetchedLive) "أسعار الذهب اللحظية بالسعودية (مباشر)" else "سوق الذهب بالريال السعودي (تقديري)"
      )
    } catch (e: Exception) {
      val p24 = 334.50
      GoldRates(
        price24kPerGram = p24,
        price22kPerGram = Math.round((p24 * (22.0 / 24.0)) * 100.0) / 100.0,
        price21kPerGram = Math.round((p24 * (21.0 / 24.0)) * 100.0) / 100.0,
        price18kPerGram = Math.round((p24 * (18.0 / 24.0)) * 100.0) / 100.0,
        currency = "SAR",
        lastUpdatedTimestamp = System.currentTimeMillis(),
        isLive = false,
        marketSource = "سوق الذهب بالريال السعودي"
      )
    }
  }
}
