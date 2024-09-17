fun main() {
    var keepRunning = true
    val tradingLogic = TradingLogic()

    while (keepRunning) {

        tradingLogic.process()

        // Sleep for 60 seconds before fetching the next price
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
