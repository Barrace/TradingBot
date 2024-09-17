import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText("Hello, World!")
            }

            post("/submit") {
                val parameters = call.receiveParameters()
                val userInput = parameters["userInput"] ?: "No input provided"
                call.respondText("Received input: $userInput")
            }
        }
    }.start(wait = true)
}