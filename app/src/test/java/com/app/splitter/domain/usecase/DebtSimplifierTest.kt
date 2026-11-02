package com.app.splitter.domain.usecase

import com.app.splitter.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Test

class DebtSimplifierTest {

    private val userA = User("A", "Alice", "alice@example.com")
    private val userB = User("B", "Bob", "bob@example.com")
    private val userC = User("C", "Charlie", "charlie@example.com")
    private val usersMap = listOf(userA, userB, userC).associateBy { it.id }

    @Test
    fun `simple two person debt`() {
        val netBalances = mapOf(
            "A" to -20.0,
            "B" to 20.0
        )

        val debtPairs = DebtSimplifier.simplifyDebts(netBalances, usersMap)

        assertEquals(1, debtPairs.size)
        assertEquals("A", debtPairs[0].fromUser.id)
        assertEquals("B", debtPairs[0].toUser.id)
        assertEquals(20.0, debtPairs[0].amount, 0.001)
    }

    @Test
    fun `transitive chain debt simplifies from A to C directly`() {
        // A owes B $10, B owes C $10
        // Net balance: A: -10, B: 0, C: +10
        val netBalances = mapOf(
            "A" to -10.0,
            "B" to 0.0,
            "C" to 10.0
        )

        val debtPairs = DebtSimplifier.simplifyDebts(netBalances, usersMap)

        // B has net 0, so A pays C directly!
        assertEquals(1, debtPairs.size)
        assertEquals("A", debtPairs[0].fromUser.id)
        assertEquals("C", debtPairs[0].toUser.id)
        assertEquals(10.0, debtPairs[0].amount, 0.001)
    }

    @Test
    fun `circular debt reduces to zero transactions`() {
        // A owes B $20, B owes C $20, C owes A $20
        // Net balance for everyone is 0!
        val netBalances = mapOf(
            "A" to 0.0,
            "B" to 0.0,
            "C" to 0.0
        )

        val debtPairs = DebtSimplifier.simplifyDebts(netBalances, usersMap)

        assertEquals(0, debtPairs.size)
    }

    @Test
    fun `complex 3 person group balances simplify efficiently`() {
        // A: -50 (owes 50), B: -30 (owes 30), C: +80 (owed 80)
        val netBalances = mapOf(
            "A" to -50.0,
            "B" to -30.0,
            "C" to 80.0
        )

        val debtPairs = DebtSimplifier.simplifyDebts(netBalances, usersMap)

        assertEquals(2, debtPairs.size)
        val sumSettled = debtPairs.sumOf { it.amount }
        assertEquals(80.0, sumSettled, 0.001)
    }
}
