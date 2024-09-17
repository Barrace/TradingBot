import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {

    // Fire up web server
    embeddedServer(Netty, host = "0.0.0.0", port = 8080) {
        module() // Call the routing function from KtorServerRequests.kt
    }.start(wait = true)

}
