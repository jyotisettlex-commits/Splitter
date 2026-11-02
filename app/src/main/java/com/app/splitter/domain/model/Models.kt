package com.app.splitter.domain.model

enum class ExpenseCategory(val displayName: String, val iconName: String) {
    DINING("Dining", "restaurant"),
    GROCERIES("Groceries", "shopping_cart"),
    RENT("Rent & Bills", "home"),
    TRANSPORTATION("Transportation", "directions_car"),
    ENTERTAINMENT("Entertainment", "movie"),
    UTILITIES("Utilities", "lightbulb"),
    GENERAL("General", "receipt")
}

enum class GroupCategory(val displayName: String) {
    HOME("Home"),
    TRIP("Trip"),
    COUPLE("Couple"),
    EVENT("Event"),
    OTHER("Other")
}

enum class SplitType(val displayName: String, val description: String) {
    EQUAL("Equal", "Split equally among all selected participants"),
    EXACT("Exact Amounts", "Specify exact dollar amount for each participant"),
    PERCENTAGE("Percentages", "Specify percentage split (must sum to 100%)"),
    SHARES("Shares", "Specify ratio shares per person (e.g., 2 shares, 1 share)")
}

enum class ActivityType(val displayName: String) {
    EXPENSE_ADDED("Added Expense"),
    EXPENSE_EDITED("Edited Expense"),
    EXPENSE_DELETED("Deleted Expense"),
    SETTLED_UP("Settled Up"),
    GROUP_CREATED("Group Created"),
    FRIEND_ADDED("Friend Added")
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val avatarUrl: String = "",
    val isCurrentUser: Boolean = false
)

data class Group(
    val id: String,
    val name: String,
    val category: GroupCategory,
    val createdDate: Long,
    val createdByUserId: String,
    val memberIds: List<String> = emptyList()
)

data class ExpensePayer(
    val userId: String,
    val amountPaid: Double
)

data class ExpenseSplit(
    val userId: String,
    val amountOwed: Double,
    val percentage: Double? = null,
    val shares: Int? = null
)

data class Expense(
    val id: String,
    val description: String,
    val amount: Double,
    val currencySymbol: String = "$",
    val date: Long,
    val groupId: String? = null,
    val createdUserId: String,
    val splitType: SplitType = SplitType.EQUAL,
    val category: ExpenseCategory = ExpenseCategory.GENERAL,
    val isSettlement: Boolean = false,
    val payers: List<ExpensePayer> = emptyList(),
    val splits: List<ExpenseSplit> = emptyList()
)

data class ActivityLog(
    val id: String,
    val type: ActivityType,
    val title: String,
    val subtitle: String,
    val timestamp: Long,
    val groupId: String? = null,
    val relatedUserId: String? = null,
    val expenseId: String? = null
)

data class UserSettings(
    val id: Int = 1,
    val currencySymbol: String = "$",
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)

data class BalanceSummary(
    val totalYouOwe: Double = 0.0,
    val totalYouAreOwed: Double = 0.0,
    val netBalance: Double = 0.0
)

data class DebtPair(
    val fromUser: User,
    val toUser: User,
    val amount: Double
)

data class GroupBalanceSummary(
    val group: Group,
    val members: List<User>,
    val totalSpend: Double,
    val userNetBalance: Double,
    val debtPairs: List<DebtPair>
)

data class FriendBalanceSummary(
    val friend: User,
    val netBalance: Double, // positive = friend owes you, negative = you owe friend
    val debtPairs: List<DebtPair>
)
