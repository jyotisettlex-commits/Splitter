package com.app.splitter.domain.usecase

import kotlin.math.roundToInt

object CurrencyConverter {

    private val ratesToUsd = mapOf(
        "$" to 1.0,
        "€" to 0.92,
        "£" to 0.79,
        "₹" to 86.50
    )

    fun convert(amount: Double, fromSymbol: String, toSymbol: String): Double {
        if (fromSymbol == toSymbol) return amount
        val fromRate = ratesToUsd[fromSymbol] ?: 1.0
        val toRate = ratesToUsd[toSymbol] ?: 1.0

        // Amount in USD = amount / fromRate
        val amountInUsd = amount / fromRate
        val converted = amountInUsd * toRate

        return (converted * 100).roundToInt() / 100.0
    }

    fun formatAmount(amount: Double, currencySymbol: String): String {
        return "$currencySymbol${String.format("%.2f", amount)}"
    }
}
