package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import utils.LogManager
import kotlinx.coroutines.channels.consumeEach
import utils.LogManager.log
import utils.LogManager.updateClients

val activeSessions = mutableListOf<DefaultWebSocketSession>()

fun Route.logRoutes() {

    // Serve logs via HTTP
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

    // WebSocket endpoint for real-time log updates
    webSocket("/log-updates") {
        activeSessions.add(this)
        try {
            send(LogManager.getLogs().joinToString(separator = "\n"))

            // Handle incoming WebSocket messages
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    when (frame.readText()) {
                        "stop" -> {
                            tradingLogic.isTrading = false
                            log("Trade stopped by the user.\n\n\n")
                            updateClients()

                            send("redirect")
                        }
                    }
                }
            }
        } finally {
            activeSessions.remove(this)
        }
    }
}
