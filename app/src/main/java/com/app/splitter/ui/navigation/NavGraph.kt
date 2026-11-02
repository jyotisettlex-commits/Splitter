package com.app.splitter.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.splitter.ui.screens.activity.ActivityScreen
import com.app.splitter.ui.screens.expense.AddExpenseScreen
import com.app.splitter.ui.screens.friends.AddFriendScreen
import com.app.splitter.ui.screens.friends.FriendDetailScreen
import com.app.splitter.ui.screens.friends.FriendsScreen
import com.app.splitter.ui.screens.groups.CreateGroupScreen
import com.app.splitter.ui.screens.groups.GroupDetailScreen
import com.app.splitter.ui.screens.groups.GroupsScreen
import com.app.splitter.ui.screens.profile.ProfileScreen
import com.app.splitter.ui.screens.settle.SettleUpScreen

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = bottomNavScreens.any { it.route == currentRoute }


    Scaffold(
        bottomBar = {

            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    bottomNavScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isTopLevelDestination && currentRoute != Screen.Groups.route && currentRoute != Screen.Friends.route) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddExpense.createRoute()) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Groups.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Groups Tab
            composable(Screen.Groups.route) {
                GroupsScreen(
                    onGroupClick = { groupId -> navController.navigate(Screen.GroupDetail.createRoute(groupId)) },
                    onCreateGroupClick = { navController.navigate(Screen.CreateGroup.route) }
                )
            }

            // Group Detail
            composable(
                route = Screen.GroupDetail.route,
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) {
                GroupDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddExpenseClick = { groupId -> navController.navigate(Screen.AddExpense.createRoute(groupId = groupId)) },
                    onSettleUpClick = { groupId -> navController.navigate(Screen.SettleUp.createRoute(groupId = groupId)) },
                    onEditExpenseClick = { expenseId -> navController.navigate(Screen.AddExpense.createRoute(expenseId = expenseId)) }
                )
            }

            // Create Group
            composable(Screen.CreateGroup.route) {
                CreateGroupScreen(
                    onBackClick = { navController.popBackStack() },
                    onGroupCreated = { navController.popBackStack() }
                )
            }

            // Friends Tab
            composable(Screen.Friends.route) {
                FriendsScreen(
                    onFriendClick = { friendId -> navController.navigate(Screen.FriendDetail.createRoute(friendId)) },
                    onAddFriendClick = { navController.navigate(Screen.AddFriend.route) }
                )
            }

            // Friend Detail
            composable(
                route = Screen.FriendDetail.route,
                arguments = listOf(navArgument("friendId") { type = NavType.StringType })
            ) {
                FriendDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddExpenseClick = { friendId -> navController.navigate(Screen.AddExpense.createRoute(friendId = friendId)) },
                    onSettleUpClick = { friendId -> navController.navigate(Screen.SettleUp.createRoute(friendId = friendId)) },
                    onEditExpenseClick = { expenseId -> navController.navigate(Screen.AddExpense.createRoute(expenseId = expenseId)) }
                )
            }

            // Add Friend
            composable(Screen.AddFriend.route) {
                AddFriendScreen(
                    onBackClick = { navController.popBackStack() },
                    onFriendAdded = { navController.popBackStack() }
                )
            }

            // Activity Tab
            composable(Screen.Activity.route) {
                ActivityScreen()
            }

            // Profile / Account Tab
            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            // Add Expense Flow
            composable(
                route = Screen.AddExpense.route,
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("friendId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("expenseId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) {
                AddExpenseScreen(
                    onBackClick = { navController.popBackStack() },
                    onExpenseSaved = { navController.popBackStack() }
                )
            }

            // Settle Up Flow
            composable(
                route = Screen.SettleUp.route,
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("friendId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) {
                SettleUpScreen(
                    onBackClick = { navController.popBackStack() },
                    onSettlementSaved = { navController.popBackStack() }
                )
            }
        }
    }
}
