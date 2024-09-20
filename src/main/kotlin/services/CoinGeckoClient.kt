package services

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class CoinGeckoClient {
    private val client = OkHttpClient.Builder()
        .cache(null) // Ensure caching is disabled
        .build()

    fun getBitcoinPrice(): Double? {
        // Add a cache-busting parameter to the URL
        val url = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd&cb=${System.currentTimeMillis()}"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Failed to fetch data: ${response.code}")
                return null
            }

            val responseBody = response.body?.string()
            if (responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                return jsonObject.getJSONObject("bitcoin").getDouble("usd")
            }
        }

        return null
    }
}
