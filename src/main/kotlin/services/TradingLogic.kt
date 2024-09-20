package services

import models.Candle
import utils.Globals
import utils.LogManager.log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TradingLogic {

    private val coinGeckoClient = CoinGeckoClient()
    private val heikinAshiCalculator = HeikinAshiCalculator()
    var isTrading = false

    /**
     * Continuously processes trading based on the set fire rate.
     */
    fun executeTradingLoop() {
        while (isTrading) {
            process()

            // Execute trading logic every utils.Globals.fireRate seconds
            repeat(Globals.fireRate) {
                if (!isTrading) return // Exit if trading is stopped
                Thread.sleep(1000) // Sleep for 1 second
            }
        }
    }

    /**
     * Processes the trading logic by retrieving the current asset price,
     * calculating the Heikin-Ashi candle, and making buy/sell decisions.
     */
    private fun process() {
        val bitcoinPrice = coinGeckoClient.getBitcoinPrice()

        if (bitcoinPrice != null) {
            // Assume the current price is the open, high, low, and close (since we're using only one price point)
            val candle = Candle(bitcoinPrice, bitcoinPrice, bitcoinPrice, bitcoinPrice, false)

            val haCandle = heikinAshiCalculator.calculateHeikinAshiCandle(candle)
            log("Heikin-Ashi Candle - Open: ${haCandle.open}, High: ${haCandle.high}, Low: ${haCandle.low}, Close: ${haCandle.close} | ${getCurrentDateTime()}")

            val buyingPower = Globals.buyingPower
            val sellingPower = Globals.sellingPower
            val wasGreen = Globals.wasGreen
            val asset = Globals.asset

            when {
                wasGreen && !haCandle.isGreen -> log("Switched from Green to Red")
                !wasGreen && haCandle.isGreen -> log("Switched from Red to Green")
            }

            if (haCandle.isGreen && !wasGreen && buyingPower > 0) {
                executeBuy(bitcoinPrice, buyingPower, asset)
            }

            if (!haCandle.isGreen && wasGreen && sellingPower > 0) {
                executeSell(bitcoinPrice, sellingPower, asset)
            }

            Globals.wasGreen = haCandle.isGreen
        } else {
            log("Failed to retrieve Bitcoin price from CoinGecko")
        }
    }

    private fun executeBuy(assetPrice: Double, buyingPower: Double, asset: String) {
        val buyAmountInBtc = buyingPower / assetPrice
        log(
            """
                
                
            BOUGHT $buyAmountInBtc $asset at $assetPrice
            Total Portfolio Value: $buyingPower USD
            ${getCurrentDateTime()}
            
            
            """.trimIndent()
        )

        // Update global buying and selling power
        Globals.buyingPower -= buyingPower
        Globals.sellingPower += buyAmountInBtc
    }

    private fun executeSell(assetPrice: Double, sellingPower: Double, asset: String) {
        val sellAmountInUsd = sellingPower * assetPrice
        log(
            """
                
                
            SOLD $sellingPower $asset at $assetPrice
            Total Portfolio Value: $sellAmountInUsd USD
            ${getCurrentDateTime()}
            
            
            """.trimIndent()
        )

        Globals.buyingPower += sellAmountInUsd
        Globals.sellingPower -= sellingPower
    }

    /**
     * Current system date and time.
     */
    private fun getCurrentDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentDateTime.format(formatter)
    }
}
