package com.app.splitter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.splitter.domain.model.BalanceSummary
import com.app.splitter.domain.model.ExpenseCategory
import com.app.splitter.ui.theme.OweRed
import com.app.splitter.ui.theme.OweRedLight
import com.app.splitter.ui.theme.OwedGreen
import com.app.splitter.ui.theme.OwedGreenLight
import com.app.splitter.ui.theme.PrimaryGreen

@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = (size.value * 0.4).sp
        )
    }
}

@Composable
fun BalanceHeaderCard(
    summary: BalanceSummary,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val net = summary.netBalance
                    val netText = when {
                        net > 0 -> "You are owed $currencySymbol${String.format("%.2f", net)}"
                        net < 0 -> "You owe $currencySymbol${String.format("%.2f", -net)}"
                        else -> "Settled up"
                    }
                    val netColor = when {
                        net > 0 -> OwedGreen
                        net < 0 -> OweRed
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = netText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = netColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // You Owe
                Surface(
                    modifier = Modifier.weight(1f).padding(end = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = OweRedLight
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "You owe",
                            style = MaterialTheme.typography.labelMedium,
                            color = OweRed
                        )
                        Text(
                            text = "$currencySymbol${String.format("%.2f", summary.totalYouOwe)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OweRed
                        )
                    }
                }

                // You are owed
                Surface(
                    modifier = Modifier.weight(1f).padding(start = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = OwedGreenLight
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "You are owed",
                            style = MaterialTheme.typography.labelMedium,
                            color = OwedGreen
                        )
                        Text(
                            text = "$currencySymbol${String.format("%.2f", summary.totalYouAreOwed)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OwedGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyText(
    amount: Double,
    currencySymbol: String,
    prefixText: String = "",
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    modifier: Modifier = Modifier
) {
    val text = when {
        amount > 0.01 -> "$prefixText $currencySymbol${String.format("%.2f", amount)}"
        amount < -0.01 -> "$prefixText $currencySymbol${String.format("%.2f", -amount)}"
        else -> "settled up"
    }

    val color = when {
        amount > 0.01 -> OwedGreen
        amount < -0.01 -> OweRed
        else -> MaterialTheme.colorScheme.outline
    }

    Text(
        text = text,
        style = style,
        color = color,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun CategoryIcon(
    category: ExpenseCategory,
    size: Dp = 24.dp,
    tint: Color = PrimaryGreen
) {
    val icon: ImageVector = when (category) {
        ExpenseCategory.DINING -> Icons.Default.Restaurant
        ExpenseCategory.GROCERIES -> Icons.Default.ShoppingCart
        ExpenseCategory.RENT -> Icons.Default.Home
        ExpenseCategory.TRANSPORTATION -> Icons.Default.DirectionsCar
        ExpenseCategory.ENTERTAINMENT -> Icons.Default.Movie
        ExpenseCategory.UTILITIES -> Icons.Default.Lightbulb
        ExpenseCategory.GENERAL -> Icons.Default.Receipt
    }
    Icon(
        imageVector = icon,
        contentDescription = category.displayName,
        modifier = Modifier.size(size),
        tint = tint
    )
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}
