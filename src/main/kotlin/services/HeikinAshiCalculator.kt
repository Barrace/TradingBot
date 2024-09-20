package services

import models.Candle

class HeikinAshiCalculator {
    private var previousHaOpen: Double = 0.0
    private var previousHaClose: Double = 0.0
    private var initialized: Boolean = false

    fun resetForNewTrade() {
        previousHaOpen = 0.0
        previousHaClose = 0.0
        initialized = false
    }

    fun calculateHeikinAshiCandle(candle: Candle): Candle {
        val haClose = (candle.open + candle.high + candle.low + candle.close) / 4

        val haOpen = if (!initialized) {
            initialized = true
            (candle.open + candle.close) / 2
        } else {
            (previousHaOpen + previousHaClose) / 2
        }

        val haHigh = maxOf(candle.high, haOpen, haClose)
        val haLow = minOf(candle.low, haOpen, haClose)

        val isGreen = haClose > haOpen

        // Store candle open and close for the next calculation
        previousHaOpen = haOpen
        previousHaClose = haClose

        return Candle(haOpen, haHigh, haLow, haClose, isGreen)
    }
}
