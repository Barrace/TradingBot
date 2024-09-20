package app

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import routes.baseRoutes
import routes.logRoutes
import routes.tradeRoutes
import java.time.Duration

fun main() {
    // Fire up web server (use env var for web hosting)
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(Netty, host = "0.0.0.0", port = port) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
    }

    routing {
        baseRoutes()
        tradeRoutes()
        logRoutes()
    }
}
