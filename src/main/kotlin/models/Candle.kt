package models

data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val isGreen: Boolean
)