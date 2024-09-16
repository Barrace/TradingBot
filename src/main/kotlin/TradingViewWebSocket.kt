import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class TradingViewWebSocket : WebSocketListener() {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun start() {
        val request = Request.Builder()
            .url("wss://data.tradingview.com/socket.io/websocket")
            .build()
        webSocket = client.newWebSocket(request, this)
        client.dispatcher.executorService.shutdown()
    }

    fun stop() {
        webSocket?.close(1000, "User requested exit")
        println("WebSocket connection closed.")
    }
    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
        println("Connected to TradingView")

        // Example of sending a subscribe message for Bitcoin (BTCUSD) to TradingView
        val subscribeMessage = """
            {"m":"chart_create_session","p":["session_id"]}
            {"m":"quote_add_symbols","p":["session_id","BTCUSD"]}
        """.trimIndent()
        webSocket.send(subscribeMessage)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("Received message: $text")

        // Parse the response to get Bitcoin price updates (needs adjustment based on response format)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        println("Received message: ${bytes.hex()}")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        println("Error: ${t.message}")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        println("Disconnected from TradingView: $reason")
    }
}

fun main() {
    val tradingViewWebSocket = TradingViewWebSocket()
    tradingViewWebSocket.start()
}
