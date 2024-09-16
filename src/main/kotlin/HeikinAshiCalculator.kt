data class Candle(val open: Double, val high: Double, val low: Double, val close: Double, val isGreen: Boolean)

class HeikinAshiCalculator {
    private var previousHaOpen: Double = 0.0
    private var previousHaClose: Double = 0.0
    private var initialized = false

    // Function to calculate Heikin-Ashi candle
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

        // Store the current HA Open and Close for the next calculation
        previousHaOpen = haOpen
        previousHaClose = haClose

        return Candle(haOpen, haHigh, haLow, haClose, isGreen)
    }
}
