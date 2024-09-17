import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    var keepRunning = true
    val tradingLogic = TradingLogic()

    // Fire up web server
    embeddedServer(Netty, host = "0.0.0.0", port = 8080) {
        module() // Call the routing function from KtorServerRequests.kt
    }.start(wait = false)

    while (keepRunning) {

        tradingLogic.process()

        // Run once per 60sec
        for (i in 1..60) {
            if (System.`in`.available() > 0) {
                readlnOrNull()
                keepRunning = false
                break
            }
            Thread.sleep(1000) // Sleep for 1 second
        }
    }
}
