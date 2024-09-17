import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import java.time.Duration
import io.ktor.server.websocket.*


// A global scope to handle background coroutines (use with caution in production)
val tradeScope = CoroutineScope(Dispatchers.Default)

// List to hold active WebSocket connections
val activeSessions = mutableListOf<DefaultWebSocketSession>()

fun Application.module() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15) // Ping interval for keeping the connection alive
    }
    routing {
        // Serve the User Input HTML file
        get("/form") {
            call.respondText(this::class.java.classLoader.getResource("UserInput.html")!!.readText(), contentType = io.ktor.http.ContentType.Text.Html)
        }

        // Serve the logs page
        get("/logs") {
            val logContent = LogManager.getLogs().joinToString(separator = "\n")
            val logsHtml = this::class.java.classLoader.getResource("TradeLogs.html")?.readText()?.replace("{{LOGS}}", logContent)

            if (logsHtml != null) {
                call.respondText(logsHtml, contentType = io.ktor.http.ContentType.Text.Html)
            } else {
                call.respondText("Error: TradeLogs.html not found.", contentType = io.ktor.http.ContentType.Text.Plain)
            }
        }

        // Handle form submission
        post("/submit") {
            val parameters = call.receiveParameters()

            // Retrieve form data
            val provider = parameters["provider"] ?: "Unknown"
            val asset = parameters["asset"] ?: "Unknown"
            val tradeLength = parameters["tradeLength"] ?: "Unknown"
            val usdAllocated = parameters["usdAllocated"] ?: "0"

            // Process form data
            when (tradeLength) {
                "Day Trade (Not recommended)" -> Globals.fireRate = 3600 // hourly candles
                "Less than a year" -> Globals.fireRate = 86400 // daily candles
                "Years (Safest)" -> Globals.fireRate = 604800 // weekly candles
            }

            Globals.buyingPower = usdAllocated.toDouble()
            Globals.provider = provider
            Globals.asset = asset

            // Log the form data
            LogManager.addLog("Provider: $provider")
            LogManager.addLog("Asset: $asset")
            LogManager.addLog("Trade Length: $tradeLength")
            LogManager.addLog("USD Allocated to TradingBot: $usdAllocated")
            LogManager.addLog("\n")

            // Send the new log messages to all connected WebSocket clients
            updateClients()

            // Launch the trading loop in a background coroutine
            tradeScope.launch {
                TradingLogic().executeTradingLoop()
            }

            // Redirect to the logs page
            call.respondRedirect("/logs")
        }

        // WebSocket endpoint for real-time log updates
        webSocket("/log-updates") {
            activeSessions.add(this)
            try {
                // Send the current logs to the client when they connect
                val currentLogs = LogManager.getLogs().joinToString(separator = "\n")
                send(currentLogs)

                // Listen for incoming messages (if necessary)
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        // Handle incoming messages from clients if needed
                    }
                }
            } finally {
                // Remove the session when it closes
                activeSessions.remove(this)
            }
        }
    }
}

fun updateClients() {
    val logContent = LogManager.getLogs().joinToString(separator = "\n")
    tradeScope.launch {
        activeSessions.forEach { session ->
            session.send(logContent)
        }
    }
}