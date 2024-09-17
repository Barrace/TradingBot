import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*

fun Application.module() {
    routing {
        // Serve the HTML file
        get("/form") {
            call.respondText(this::class.java.classLoader.getResource("UserInput.html")!!.readText(), contentType = io.ktor.http.ContentType.Text.Html)
        }

        // Handle form submission
        post("/submit") {
            val parameters = call.receiveParameters()

            // Retrieve form data
            val provider = parameters["provider"] ?: "Unknown"
            val asset = parameters["asset"] ?: "Unknown"
            val tradeLength = parameters["tradeLength"] ?: "Unknown"
            val usdAllocated = parameters["usdAllocated"] ?: "0"

            // Process the form data (for demonstration, just returning the values)
            call.respondText("""
                Provider: $provider
                Asset: $asset
                Trade Length: $tradeLength
                USD Allocated to TradingBot: $usdAllocated
            """.trimIndent())
        }
    }
}