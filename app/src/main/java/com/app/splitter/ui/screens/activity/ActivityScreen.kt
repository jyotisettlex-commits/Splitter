package com.app.splitter.ui.screens.activity

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.splitter.domain.model.ActivityLog
import com.app.splitter.domain.model.ActivityType
import com.app.splitter.ui.components.EmptyStateView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recent Activity", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Filter Chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.filter == ActivityFilter.ALL,
                                onClick = { viewModel.setFilter(ActivityFilter.ALL) },
                                label = { Text("All Activity") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filter == ActivityFilter.BY_GROUP,
                                onClick = { viewModel.setFilter(ActivityFilter.BY_GROUP) },
                                label = { Text("By Group") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.filter == ActivityFilter.BY_PERSON,
                                onClick = { viewModel.setFilter(ActivityFilter.BY_PERSON) },
                                label = { Text("By Person") }
                            )
                        }
                    }

                    if (uiState.activities.isEmpty()) {
                        EmptyStateView(
                            title = "No Activity Logs",
                            subtitle = "Expenses, settlements, and group creation events will appear here!",
                            icon = Icons.Default.History,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(uiState.activities, key = { it.id }) { log ->
                                ActivityLogItem(log = log)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityLogItem(log: ActivityLog) {
    val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(log.timestamp))

    val (icon: ImageVector, iconBgColor) = when (log.type) {
        ActivityType.EXPENSE_ADDED -> Icons.Default.Add to MaterialTheme.colorScheme.primaryContainer
        ActivityType.EXPENSE_EDITED -> Icons.Default.Edit to MaterialTheme.colorScheme.tertiaryContainer
        ActivityType.EXPENSE_DELETED -> Icons.Default.Delete to MaterialTheme.colorScheme.errorContainer
        ActivityType.SETTLED_UP -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.secondaryContainer
        ActivityType.GROUP_CREATED -> Icons.Default.Group to MaterialTheme.colorScheme.primaryContainer
        ActivityType.FRIEND_ADDED -> Icons.Default.Person to MaterialTheme.colorScheme.secondaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBgColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = log.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
