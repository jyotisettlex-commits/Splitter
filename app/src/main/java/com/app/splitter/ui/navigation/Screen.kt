package com.app.splitter.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    // Bottom Nav Tabs
    object Groups : Screen("groups", "Groups", Icons.Default.Group)
    object Friends : Screen("friends", "Friends", Icons.Default.Person)
    object Activity : Screen("activity", "Activity", Icons.Default.History)
    object Profile : Screen("profile", "Account", Icons.Default.PersonAdd)

    // Detail & Action Screens
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }

    object CreateGroup : Screen("create_group")

    object FriendDetail : Screen("friend_detail/{friendId}") {
        fun createRoute(friendId: String) = "friend_detail/$friendId"
    }

    object AddFriend : Screen("add_friend")

    object AddExpense : Screen("add_expense?groupId={groupId}&friendId={friendId}&expenseId={expenseId}") {
        fun createRoute(groupId: String? = null, friendId: String? = null, expenseId: String? = null): String {
            val params = mutableListOf<String>()
            if (!groupId.isNullOrEmpty()) params.add("groupId=$groupId")
            if (!friendId.isNullOrEmpty()) params.add("friendId=$friendId")
            if (!expenseId.isNullOrEmpty()) params.add("expenseId=$expenseId")
            return if (params.isEmpty()) "add_expense" else "add_expense?${params.joinToString("&")}"
        }
    }

    object SettleUp : Screen("settle_up?groupId={groupId}&friendId={friendId}") {
        fun createRoute(groupId: String? = null, friendId: String? = null): String {
            val params = mutableListOf<String>()
            if (!groupId.isNullOrEmpty()) params.add("groupId=$groupId")
            if (!friendId.isNullOrEmpty()) params.add("friendId=$friendId")
            return if (params.isEmpty()) "settle_up" else "settle_up?${params.joinToString("&")}"
        }
    }
}

val bottomNavScreens = listOf(
    Screen.Groups,
    Screen.Friends,
    Screen.Activity,
    Screen.Profile
)
