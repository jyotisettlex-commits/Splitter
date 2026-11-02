package com.app.splitter.ui.screens.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.splitter.ui.components.CurrencyText
import com.app.splitter.ui.components.EmptyStateView
import com.app.splitter.ui.components.UserAvatar
import com.app.splitter.ui.screens.groups.ExpenseListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    onBackClick: () -> Unit,
    onAddExpenseClick: (friendId: String) -> Unit,
    onSettleUpClick: (friendId: String) -> Unit,
    onEditExpenseClick: (expenseId: String) -> Unit,
    viewModel: FriendDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.summary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(summary?.friend?.name ?: "Friend Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddExpenseClick(viewModel.friendId) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading || summary == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Friend Header Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    UserAvatar(name = summary.friend.name, size = 56.dp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = summary.friend.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = summary.friend.email,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        if (summary.friend.phone.isNotEmpty()) {
                                            Text(
                                                text = summary.friend.phone,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        val net = summary.netBalance
                                        val statusStr = when {
                                            net > 0.01 -> "${summary.friend.name} owes you"
                                            net < -0.01 -> "You owe ${summary.friend.name}"
                                            else -> "You and ${summary.friend.name} are settled up"
                                        }
                                        Text(
                                            text = statusStr,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        if (net != 0.0) {
                                            CurrencyText(
                                                amount = net,
                                                currencySymbol = uiState.userSettings.currencySymbol,
                                                style = MaterialTheme.typography.headlineSmall
                                            )
                                        } else {
                                            Text(
                                                text = "${uiState.userSettings.currencySymbol} 0.00",
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onSettleUpClick(viewModel.friendId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Settle Up")
                                    }
                                }
                            }
                        }
                    }

                    // Shared Expenses List Header
                    item {
                        Text(
                            text = "Shared Expenses History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    if (uiState.expenses.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Shared Expenses",
                                subtitle = "Tap '+' to add an expense with ${summary.friend.name}!",
                                icon = Icons.Default.Receipt
                            )
                        }
                    } else {
                        items(uiState.expenses, key = { it.id }) { expense ->
                            ExpenseListItem(
                                expense = expense,
                                usersMap = uiState.usersMap,
                                currentUser = uiState.currentUser,
                                currencySymbol = uiState.userSettings.currencySymbol,
                                onEditClick = { onEditExpenseClick(expense.id) },
                                onDeleteClick = { viewModel.deleteExpense(expense.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
