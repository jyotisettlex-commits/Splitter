package com.app.splitter.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyConverterTest {

    @Test
    fun `same currency conversion returns same amount`() {
        val amount = 50.0
        val converted = CurrencyConverter.convert(amount, "$", "$")
        assertEquals(50.0, converted, 0.001)
    }

    @Test
    fun `usd to eur conversion uses rate correctly`() {
        val usdAmount = 100.0
        val eurAmount = CurrencyConverter.convert(usdAmount, "$", "€")
        assertEquals(92.00, eurAmount, 0.001)
    }

    @Test
    fun `usd to inr conversion uses rate correctly`() {
        val usdAmount = 10.0
        val inrAmount = CurrencyConverter.convert(usdAmount, "$", "₹")
        assertEquals(865.00, inrAmount, 0.001)
    }
}
