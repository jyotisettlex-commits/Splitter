package com.app.splitter.domain.usecase

import com.app.splitter.domain.model.ExpenseSplit
import com.app.splitter.domain.model.SplitType
import kotlin.math.roundToInt

object SplitCalculator {

    fun calculateSplits(
        totalAmount: Double,
        participantIds: List<String>,
        splitType: SplitType,
        customExacts: Map<String, Double> = emptyMap(),
        customPercentages: Map<String, Double> = emptyMap(),
        customShares: Map<String, Int> = emptyMap()
    ): List<ExpenseSplit> {
        if (participantIds.isEmpty() || totalAmount <= 0.0) return emptyList()

        val roundedTotal = roundToCents(totalAmount)

        return when (splitType) {
            SplitType.EQUAL -> calculateEqualSplits(roundedTotal, participantIds)
            SplitType.EXACT -> calculateExactSplits(roundedTotal, participantIds, customExacts)
            SplitType.PERCENTAGE -> calculatePercentageSplits(roundedTotal, participantIds, customPercentages)
            SplitType.SHARES -> calculateSharesSplits(roundedTotal, participantIds, customShares)
        }
    }

    private fun calculateEqualSplits(
        totalAmount: Double,
        participantIds: List<String>
    ): List<ExpenseSplit> {
        val count = participantIds.size
        val totalCents = (totalAmount * 100).roundToInt()
        val baseCentsPerPerson = totalCents / count
        val remainderCents = totalCents % count

        val equalPercentage = 100.0 / count

        return participantIds.mapIndexed { index, userId ->
            // Distribute the remainder cents (+1 cent each) to the first 'remainderCents' participants
            val cents = baseCentsPerPerson + if (index < remainderCents) 1 else 0
            val amountOwed = cents / 100.0
            ExpenseSplit(
                userId = userId,
                amountOwed = amountOwed,
                percentage = equalPercentage,
                shares = 1
            )
        }
    }

    private fun calculateExactSplits(
        totalAmount: Double,
        participantIds: List<String>,
        customExacts: Map<String, Double>
    ): List<ExpenseSplit> {
        return participantIds.map { userId ->
            val exact = customExacts[userId] ?: 0.0
            ExpenseSplit(
                userId = userId,
                amountOwed = roundToCents(exact)
            )
        }
    }

    private fun calculatePercentageSplits(
        totalAmount: Double,
        participantIds: List<String>,
        customPercentages: Map<String, Double>
    ): List<ExpenseSplit> {
        val totalCents = (totalAmount * 100).roundToInt()
        val centsList = mutableListOf<Int>()

        var accumulatedCents = 0
        participantIds.forEach { userId ->
            val pct = customPercentages[userId] ?: (100.0 / participantIds.size)
            val calculatedCents = ((pct / 100.0) * totalCents).roundToInt()
            centsList.add(calculatedCents)
            accumulatedCents += calculatedCents
        }

        val diffCents = totalCents - accumulatedCents
        if (diffCents != 0 && centsList.isNotEmpty()) {
            centsList[0] = centsList[0] + diffCents
        }

        return participantIds.mapIndexed { index, userId ->
            val pct = customPercentages[userId] ?: (100.0 / participantIds.size)
            ExpenseSplit(
                userId = userId,
                amountOwed = centsList[index] / 100.0,
                percentage = pct
            )
        }
    }

    private fun calculateSharesSplits(
        totalAmount: Double,
        participantIds: List<String>,
        customShares: Map<String, Int>
    ): List<ExpenseSplit> {
        val totalShares = participantIds.sumOf { customShares[it] ?: 1 }.coerceAtLeast(1)
        val totalCents = (totalAmount * 100).roundToInt()

        var accumulatedCents = 0
        val centsList = mutableListOf<Int>()

        participantIds.forEach { userId ->
            val shares = customShares[userId] ?: 1
            val cents = ((shares.toDouble() / totalShares) * totalCents).roundToInt()
            centsList.add(cents)
            accumulatedCents += cents
        }

        val diffCents = totalCents - accumulatedCents
        if (diffCents != 0 && centsList.isNotEmpty()) {
            centsList[0] = centsList[0] + diffCents
        }

        return participantIds.mapIndexed { index, userId ->
            val shares = customShares[userId] ?: 1
            ExpenseSplit(
                userId = userId,
                amountOwed = centsList[index] / 100.0,
                shares = shares
            )
        }
    }

    fun roundToCents(value: Double): Double {
        return (value * 100).roundToInt() / 100.0
    }
}
