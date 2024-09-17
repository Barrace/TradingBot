object LogManager {
    private val logMessages = mutableListOf<String>()

    // Add a log message to the list
    fun addLog(message: String) {
        logMessages.add(message)
    }

    // Retrieve the current list of log messages
    fun getLogs(): List<String> {
        return logMessages.toList() // Return a copy to prevent direct modification
    }
}
