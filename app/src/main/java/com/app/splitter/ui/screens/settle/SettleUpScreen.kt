package com.app.splitter.ui.screens.settle

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.app.splitter.ui.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpScreen(
    onBackClick: () -> Unit,
    onSettlementSaved: () -> Unit,
    viewModel: SettleUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var payerDropdownExpanded by remember { mutableStateOf(false) }
    var payeeDropdownExpanded by remember { mutableStateOf(false) }

    val payerUser = uiState.availableUsers.firstOrNull { it.id == uiState.payerId }
    val payeeUser = uiState.availableUsers.firstOrNull { it.id == uiState.payeeId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settle Up", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Record a Payment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Payment Flow Visualization: Payer -> Payee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payer Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { payerDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatar(name = payerUser?.name ?: "Payer", size = 48.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (payerUser?.id == uiState.currentUser?.id) "You" else (payerUser?.name ?: "Payer"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

                        DropdownMenu(
                            expanded = payerDropdownExpanded,
                            onDismissRequest = { payerDropdownExpanded = false }
                        ) {
                            uiState.availableUsers.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text(if (user.id == uiState.currentUser?.id) "You (${user.name})" else user.name) },
                                    onClick = {
                                        viewModel.onPayerChanged(user.id)
                                        payerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Payee Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { payeeDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatar(name = payeeUser?.name ?: "Payee", size = 48.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (payeeUser?.id == uiState.currentUser?.id) "You" else (payeeUser?.name ?: "Payee"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Received", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

                        DropdownMenu(
                            expanded = payeeDropdownExpanded,
                            onDismissRequest = { payeeDropdownExpanded = false }
                        ) {
                            uiState.availableUsers.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text(if (user.id == uiState.currentUser?.id) "You (${user.name})" else user.name) },
                                    onClick = {
                                        viewModel.onPayeeChanged(user.id)
                                        payeeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Amount Field
            OutlinedTextField(
                value = uiState.amountStr,
                onValueChange = { viewModel.onAmountChanged(it) },
                label = { Text("Settlement Amount") },
                prefix = { Text(uiState.userSettings.currencySymbol, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.confirmSettlement(onSettlementSaved) },
                enabled = !uiState.isSaving && uiState.amountStr.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm Settlement", fontWeight = FontWeight.Bold)
            }
        }
    }
}
