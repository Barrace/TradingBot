import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import java.time.Duration

val tradeScope = CoroutineScope(Dispatchers.Default)
val activeSessions = mutableListOf<DefaultWebSocketSession>()
var isTrading = false

fun Application.module() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
    }

    routing {
        // Default page
        get("/") {
            call.respondText(
                "<html><body><h1>Welcome to Barrace Trading Bot!</h1><p>Use <a href='/newTrade'>/newTrade</a> to get started</p></body></html>",
                contentType = ContentType.Text.Html
            )
        }

        // Monitor
        get("/status") {
            call.respondText("OK", ContentType.Text.Plain)
        }

        // Serve the newTrade page
        get("/newTrade") {
            call.respondText(
                this::class.java.classLoader.getResource("UserInput.html")!!.readText(),
                contentType = ContentType.Text.Html
            )
        }

        // Serve the logs page
        get("/logs") {
            val logContent = LogManager.getLogs().joinToString(separator = "\n")
            val logsHtml = this::class.java.classLoader.getResource("TradeLogs.html")
                ?.readText()
                ?.replace("{{LOGS}}", logContent)

            if (logsHtml != null) {
                call.respondText(logsHtml, contentType = ContentType.Text.Html)
            } else {
                call.respondText("Error: TradeLogs.html not found.", contentType = ContentType.Text.Plain)
            }
        }

        // Handle form submission
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
            log("USD Allocated to TradingBot: $usdAllocated \n")
            updateClients()

            isTrading = true
            tradeScope.launch {
                TradingLogic().executeTradingLoop()
            }

            call.respondRedirect("/logs")
        }

        // WebSocket endpoint for real-time log updates
        webSocket("/log-updates") {
            activeSessions.add(this)
            try {
                send(LogManager.getLogs().joinToString(separator = "\n"))
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        if (frame.readText() == "stop") {
                            isTrading = false
                            log("Trade stopped by the user. \n \n \n")
                            updateClients()

                            // Send a redirect command to the client
                            send("redirect")
                        }
                    }
                }
            } finally {
                activeSessions.remove(this)
            }
        }
    }
}

fun log(str : String) {
    println(str)
    LogManager.addLog(str)
}

private fun updateClients() {
    val logContent = LogManager.getLogs().joinToString(separator = "\n")
    tradeScope.launch {
        activeSessions.forEach { session ->
            session.send(logContent)
        }
    }
}
