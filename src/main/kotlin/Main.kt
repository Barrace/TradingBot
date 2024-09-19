import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {

    // Fire up web server (use env var for web hosting)
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, host = "0.0.0.0", port = port) {
        module() // Call the routing function from KtorServerRequests.kt
    }.start(wait = true)

}
