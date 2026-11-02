package com.app.splitter.domain.usecase

import com.app.splitter.domain.model.SplitType
import org.junit.Assert.assertEquals
import org.junit.Test

class SplitCalculatorTest {

    @Test
    fun `equal split of 10 dollars among 3 people equals exactly 10 dollars total`() {
        val total = 10.00
        val participants = listOf("user1", "user2", "user3")

        val splits = SplitCalculator.calculateSplits(
            totalAmount = total,
            participantIds = participants,
            splitType = SplitType.EQUAL
        )

        assertEquals(3, splits.size)
        // First participant gets remainder penny
        assertEquals(3.34, splits[0].amountOwed, 0.001)
        assertEquals(3.33, splits[1].amountOwed, 0.001)
        assertEquals(3.33, splits[2].amountOwed, 0.001)

        val sum = splits.sumOf { it.amountOwed }
        assertEquals(total, sum, 0.001)
    }

    @Test
    fun `equal split of 1 dollar among 3 people distributes remainder penny`() {
        val total = 1.00
        val participants = listOf("user1", "user2", "user3")

        val splits = SplitCalculator.calculateSplits(
            totalAmount = total,
            participantIds = participants,
            splitType = SplitType.EQUAL
        )

        assertEquals(0.34, splits[0].amountOwed, 0.001)
        assertEquals(0.33, splits[1].amountOwed, 0.001)
        assertEquals(0.33, splits[2].amountOwed, 0.001)

        val sum = splits.sumOf { it.amountOwed }
        assertEquals(total, sum, 0.001)
    }

    @Test
    fun `exact split uses provided exact amounts`() {
        val total = 50.00
        val participants = listOf("user1", "user2")
        val exacts = mapOf("user1" to 30.00, "user2" to 20.00)

        val splits = SplitCalculator.calculateSplits(
            totalAmount = total,
            participantIds = participants,
            splitType = SplitType.EXACT,
            customExacts = exacts
        )

        assertEquals(30.00, splits[0].amountOwed, 0.001)
        assertEquals(20.00, splits[1].amountOwed, 0.001)
    }

    @Test
    fun `percentage split calculates correctly and matches total`() {
        val total = 100.00
        val participants = listOf("user1", "user2", "user3")
        val percentages = mapOf("user1" to 50.0, "user2" to 30.0, "user3" to 20.0)

        val splits = SplitCalculator.calculateSplits(
            totalAmount = total,
            participantIds = participants,
            splitType = SplitType.PERCENTAGE,
            customPercentages = percentages
        )

        assertEquals(50.00, splits[0].amountOwed, 0.001)
        assertEquals(30.00, splits[1].amountOwed, 0.001)
        assertEquals(20.00, splits[2].amountOwed, 0.001)
        assertEquals(total, splits.sumOf { it.amountOwed }, 0.001)
    }

    @Test
    fun `shares split distributes ratio correctly`() {
        val total = 100.00
        val participants = listOf("user1", "user2")
        val shares = mapOf("user1" to 3, "user2" to 1)

        val splits = SplitCalculator.calculateSplits(
            totalAmount = total,
            participantIds = participants,
            splitType = SplitType.SHARES,
            customShares = shares
        )

        assertEquals(75.00, splits[0].amountOwed, 0.001)
        assertEquals(25.00, splits[1].amountOwed, 0.001)
        assertEquals(total, splits.sumOf { it.amountOwed }, 0.001)
    }
}
