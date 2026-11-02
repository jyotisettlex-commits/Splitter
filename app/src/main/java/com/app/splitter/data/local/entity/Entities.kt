package com.app.splitter.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val avatarUrl: String,
    val isCurrentUser: Boolean
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val createdDate: Long,
    val createdByUserId: String
)

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "userId"],
    indices = [Index("groupId"), Index("userId")]
)
data class GroupMemberEntity(
    val groupId: String,
    val userId: String
)

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val userId: String,
    val addedDate: Long
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val description: String,
    val amount: Double,
    val currencySymbol: String,
    val date: Long,
    val groupId: String?,
    val createdUserId: String,
    val splitType: String,
    val category: String,
    val isSettlement: Boolean
)

@Entity(
    tableName = "expense_payers",
    indices = [Index("expenseId"), Index("userId")]
)
data class ExpensePayerEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val userId: String,
    val amountPaid: Double
)

@Entity(
    tableName = "expense_splits",
    indices = [Index("expenseId"), Index("userId")]
)
data class ExpenseSplitEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val userId: String,
    val amountOwed: Double,
    val percentage: Double?,
    val shares: Int?
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val subtitle: String,
    val timestamp: Long,
    val groupId: String?,
    val relatedUserId: String?,
    val expenseId: String?
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val currencySymbol: String,
    val isDarkMode: Boolean,
    val notificationsEnabled: Boolean
)
