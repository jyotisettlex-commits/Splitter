package com.app.splitter.domain.usecase

import com.app.splitter.domain.model.DebtPair
import com.app.splitter.domain.model.User
import kotlin.math.roundToInt

object DebtSimplifier {

    fun simplifyDebts(
        netBalances: Map<String, Double>,
        usersMap: Map<String, User>
    ): List<DebtPair> {
        val debtors = mutableListOf<Pair<String, Int>>() // userId to cents owed (>0)
        val creditors = mutableListOf<Pair<String, Int>>() // userId to cents to receive (>0)

        for ((userId, balance) in netBalances) {
            val cents = (balance * 100).roundToInt()
            if (cents < 0) {
                debtors.add(userId to -cents)
            } else if (cents > 0) {
                creditors.add(userId to cents)
            }
        }

        debtors.sortByDescending { it.second }
        creditors.sortByDescending { it.second }

        val result = mutableListOf<DebtPair>()
        var dIdx = 0
        var cIdx = 0

        while (dIdx < debtors.size && cIdx < creditors.size) {
            val (debtorId, debtCents) = debtors[dIdx]
            val (creditorId, creditCents) = creditors[cIdx]

            val settleCents = minOf(debtCents, creditCents)

            val debtorUser = usersMap[debtorId] ?: User(debtorId, "User $debtorId", "")
            val creditorUser = usersMap[creditorId] ?: User(creditorId, "User $creditorId", "")

            result.add(
                DebtPair(
                    fromUser = debtorUser,
                    toUser = creditorUser,
                    amount = settleCents / 100.0
                )
            )

            val remainingDebt = debtCents - settleCents
            val remainingCredit = creditCents - settleCents

            if (remainingDebt == 0) {
                dIdx++
            } else {
                debtors[dIdx] = debtorId to remainingDebt
            }

            if (remainingCredit == 0) {
                cIdx++
            } else {
                creditors[cIdx] = creditorId to remainingCredit
            }
        }

        return result
    }
}
