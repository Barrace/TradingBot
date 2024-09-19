import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TradingLogic {

    val coinGeckoClient = CoinGeckoClient()
    val heikinAshiCalculator = HeikinAshiCalculator()

    fun process() {
        val bitcoinPrice = coinGeckoClient.getBitcoinPrice()
        if (bitcoinPrice != null) {
            // Assume the current price is the open, high, low, and close (since we're using only one price point)
            val candle = Candle(bitcoinPrice, bitcoinPrice, bitcoinPrice, bitcoinPrice, false)

            val haCandle = heikinAshiCalculator.calculateHeikinAshiCandle(candle)
            log("Heikin-Ashi Candle - Open: ${haCandle.open}, High: ${haCandle.high}, Low: ${haCandle.low}, Close: ${haCandle.close} | ${getCurrentDateTime()}")

            val buyingPower = Globals.buyingPower
            val sellingPower = Globals.sellingPower
            val wasGreen = Globals.wasGreen

            if(wasGreen && !haCandle.isGreen)
                log("Switched from Green to Red")

            if(!wasGreen && haCandle.isGreen)
                log("Switched from Red to Green")

            //BUY
            if (haCandle.close > haCandle.open && buyingPower > 0 && !wasGreen) {
                executeBuy(bitcoinPrice, buyingPower)
            }

            //SELL
            if (haCandle.open > haCandle.close && sellingPower > 0 && wasGreen) {
                executeSell(bitcoinPrice, sellingPower)
            }

            Globals.wasGreen = haCandle.isGreen

        } else {
            log("Failed to retrieve Bitcoin price.")
        }
    }

    fun executeTradingLoop() {
        val tradingLogic = TradingLogic()

        while (isTrading) {
            tradingLogic.process()

            // Run as often as user selected based on Globals.fireRate.
            for (i in 1..Globals.fireRate) {
                if (!isTrading) {
                    return // Exit the function
                }
                Thread.sleep(1000) // Sleep for 1 second
            }
        }
    }

    private fun executeBuy(price: Double, buyingPower: Double) {
        val buyAmountInBtc = buyingPower / price
        log("\n \n    BOUGHT $buyAmountInBtc at $price \n " +
                "    Total Portfolio Value: $buyingPower \n" +
                "    ${getCurrentDateTime()} \n \n")

        Globals.buyingPower -= buyingPower
        Globals.sellingPower += buyAmountInBtc
    }
    private fun executeSell(price: Double, sellingPower: Double) {
        val sellAmountInUsd = sellingPower * price
        log("\n \n SOLD $sellingPower at $price \n " +
                "Total Portfolio Value: $sellAmountInUsd \n" +
                "${getCurrentDateTime()} \n \n")

        Globals.buyingPower += sellAmountInUsd
        Globals.sellingPower -= sellingPower
    }

    private fun getCurrentDateTime(): String {
        // In system's default timezone
        val currentDateTime = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        return currentDateTime.format(formatter)
    }
}