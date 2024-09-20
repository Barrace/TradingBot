package routes

import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.baseRoutes() {

    // Default home page route
    get("/") {
        call.respondText(
            """
            <html>
                <body>
                    <h1>Welcome to Barrace Trading Bot!</h1>
                    <p>Use <a href='/newTrade'>/newTrade</a> to get started</p>
                </body>
            </html>
            """.trimIndent(),
            contentType = ContentType.Text.Html
        )
    }

    // Monitor route for health status
    get("/status") {
        call.respondText("OK", ContentType.Text.Plain)
    }
}
