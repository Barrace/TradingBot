package utils

import io.ktor.websocket.*
import kotlinx.coroutines.*
import routes.activeSessions

object LogManager {
    private val logMessages = mutableListOf<String>()
    val tradeScope = CoroutineScope(Dispatchers.Default)

    fun log(str: String) {
        println(str)
        addLog(str)
    }

    fun updateClients() {
        val logContent = getLogs().joinToString(separator = "\n")
        tradeScope.launch {
            activeSessions.forEach { session ->
                session.send(Frame.Text(logContent))
            }
        }
    }

    fun getLogs(): List<String> {
        return logMessages.toList() // Return a copy to prevent direct modification
    }

    private fun addLog(message: String) {
        logMessages.add(message)
    }
}
