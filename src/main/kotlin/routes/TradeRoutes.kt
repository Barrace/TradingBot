package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import utils.Globals
import kotlinx.coroutines.*
import services.TradingLogic
import utils.LogManager.log
import utils.LogManager.updateClients

val tradeScope = CoroutineScope(Dispatchers.Default)
val tradingLogic = TradingLogic()

fun Route.tradeRoutes() {

    // Serve the newTrade form page
    get("/newTrade") {
        call.respondText(
            this::class.java.classLoader.getResource("UserInput.html")!!.readText(),
            contentType = ContentType.Text.Html
        )
    }

    // Handle form submission to start a new trade
    post("/submit") {
        val parameters = call.receiveParameters()
        val provider = parameters["provider"] ?: "Unknown"
        val asset = parameters["asset"] ?: "Unknown"
        val tradeLength = parameters["tradeLength"] ?: "Unknown"
        val usdAllocated = parameters["usdAllocated"]?.toDoubleOrNull() ?: 0.0

        Globals.apply {
            this.provider = provider
            this.asset = asset
            this.buyingPower = usdAllocated
            this.fireRate = when (tradeLength) {
                "TEST FIRE RATE (1min)" -> 60
                "Day Trade (Not recommended)" -> 3600
                "Less than a year" -> 86400
                "Years (Safest)" -> 604800
                else -> 604800
            }
        }

        log("\n\n\n_______________NEW TRADE CREATED_______________")
        log("Provider: $provider")
        log("Asset: $asset")
        log("Trade Length: $tradeLength")
        log("USD Allocated to TradingBot: $usdAllocated\n")
        updateClients()

        tradingLogic.isTrading = true
        tradeScope.launch {
            tradingLogic.executeTradingLoop()
        }

        call.respondRedirect("/logs")
    }
}
