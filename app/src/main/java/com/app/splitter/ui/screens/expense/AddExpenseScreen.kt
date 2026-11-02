package com.app.splitter.ui.screens.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.splitter.domain.model.ExpenseCategory
import com.app.splitter.domain.model.SplitType
import com.app.splitter.ui.components.CategoryIcon
import com.app.splitter.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBackClick: () -> Unit,
    onExpenseSaved: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var paidByDropdownExpanded by remember { mutableStateOf(false) }
    var groupDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Edit Expense" else "Add Expense", fontWeight = FontWeight.Bold) },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Amount Input
            OutlinedTextField(
                value = uiState.amountStr,
                onValueChange = { viewModel.onAmountChanged(it) },
                label = { Text("Amount") },
                placeholder = { Text("0.00") },
                prefix = { Text(uiState.userSettings.currencySymbol, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description Input
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Description") },
                placeholder = { Text("e.g. Dinner, Groceries, Flight ticket") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Picker
            Text(
                text = "Expense Category",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ExpenseCategory.values()) { cat ->
                    val isSelected = uiState.category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(cat) },
                        label = { Text(cat.displayName) },
                        leadingIcon = { CategoryIcon(category = cat, size = 18.dp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Group or Friends selection
            Text(
                text = "Group / Participants",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clickable { groupDropdownExpanded = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val selectedGroupName = uiState.groups.firstOrNull { it.id == uiState.selectedGroupId }?.name
                    Text(
                        text = if (selectedGroupName != null) "Group: $selectedGroupName" else "Non-group expense (Individual friends)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box {
                        Text(
                            text = "Change",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        DropdownMenu(
                            expanded = groupDropdownExpanded,
                            onDismissRequest = { groupDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No Group (Non-group)") },
                                onClick = {
                                    viewModel.onGroupSelected(null)
                                    groupDropdownExpanded = false
                                }
                            )
                            uiState.groups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name) },
                                    onClick = {
                                        viewModel.onGroupSelected(group.id)
                                        groupDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.selectedGroupId == null) {
                Text(
                    text = "Select Friends involved:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                uiState.friends.forEach { friend ->
                    val isSelected = uiState.selectedFriendIds.contains(friend.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleFriendSelection(friend.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggleFriendSelection(friend.id) }
                        )
                        UserAvatar(name = friend.name, size = 30.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(friend.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Paid By Selector
            Text(
                text = "Paid By",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clickable { paidByDropdownExpanded = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val payerUser = uiState.availableParticipants.firstOrNull { it.id == uiState.paidByUserId }
                    val payerName = if (payerUser?.id == uiState.currentUser?.id) "You (${payerUser?.name})" else (payerUser?.name ?: "Select Payer")
                    Text(
                        text = "Paid by $payerName",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box {
                        Text(
                            text = "Change",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        DropdownMenu(
                            expanded = paidByDropdownExpanded,
                            onDismissRequest = { paidByDropdownExpanded = false }
                        ) {
                            uiState.availableParticipants.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text(if (user.id == uiState.currentUser?.id) "You (${user.name})" else user.name) },
                                    onClick = {
                                        viewModel.onPaidByChanged(user.id)
                                        paidByDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Split Type Selection
            Text(
                text = "Split Type",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SplitType.values()) { st ->
                    val isSelected = uiState.splitType == st
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onSplitTypeChanged(st) },
                        label = { Text(st.displayName) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Inputs based on Split Type
            when (uiState.splitType) {
                SplitType.EQUAL -> {
                    val totalAmt = uiState.amountStr.toDoubleOrNull() ?: 0.0
                    val count = uiState.availableParticipants.size.coerceAtLeast(1)
                    val share = totalAmt / count
                    Text(
                        text = "Split equally among ${uiState.availableParticipants.size} people: ${uiState.userSettings.currencySymbol}${String.format("%.2f", share)} per person",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                SplitType.EXACT -> {
                    uiState.availableParticipants.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(name = p.name, size = 32.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (p.id == uiState.currentUser?.id) "You" else p.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = uiState.exactAmounts[p.id] ?: "",
                                onValueChange = { viewModel.onExactAmountChanged(p.id, it) },
                                placeholder = { Text("0.00") },
                                prefix = { Text(uiState.userSettings.currencySymbol) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }
                SplitType.PERCENTAGE -> {
                    uiState.availableParticipants.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(name = p.name, size = 32.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (p.id == uiState.currentUser?.id) "You" else p.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = uiState.percentages[p.id] ?: "",
                                onValueChange = { viewModel.onPercentageChanged(p.id, it) },
                                placeholder = { Text("0") },
                                suffix = { Text("%") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(100.dp)
                            )
                        }
                    }
                }
                SplitType.SHARES -> {
                    uiState.availableParticipants.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(name = p.name, size = 32.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (p.id == uiState.currentUser?.id) "You" else p.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = uiState.shares[p.id] ?: "1",
                                onValueChange = { viewModel.onShareChanged(p.id, it) },
                                placeholder = { Text("1") },
                                suffix = { Text("shares") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.saveExpense(onExpenseSaved) },
                enabled = !uiState.isSaving && uiState.description.isNotBlank() && uiState.amountStr.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Expense", fontWeight = FontWeight.Bold)
            }
        }
    }
}
