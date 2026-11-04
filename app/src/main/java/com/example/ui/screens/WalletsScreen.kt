package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GoldHoldingEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.ui.components.HorizontalProgressBar
import com.example.ui.dialogs.AddGoldHoldingDialog
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.Gold24k
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletsScreen(
  viewModel: FinanceViewModel,
  modifier: Modifier = Modifier
) {
  val language by viewModel.currentLanguage.collectAsState()
  val overview by viewModel.overview.collectAsState()
  val goldHoldings by viewModel.goldHoldings.collectAsState()
  val savingsGoals by viewModel.savingsGoals.collectAsState()
  val goldRates by viewModel.goldRates.collectAsState()

  var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Gold, 1: Savings
  var showAddGoldDialog by remember { mutableStateOf(false) }
  var showAddSavingsGoalDialog by remember { mutableStateOf(false) }
  var selectedGoalForDeposit by remember { mutableStateOf<SavingsGoalEntity?>(null) }

  val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale(language.code))

  val subTabs = listOf(
    AppStrings.get("gold_portfolio", language),
    AppStrings.get("savings_portfolio", language)
  )

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      androidx.compose.material3.TabRow(
        selectedTabIndex = selectedSubTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        subTabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedSubTab == index,
            onClick = { selectedSubTab = index },
            text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
          )
        }
      }

      if (selectedSubTab == 0) {
        // Gold Portfolio Tab
        GoldPortfolioTab(
          holdings = goldHoldings,
          rates = goldRates,
          overview = overview,
          language = language,
          dateFormat = dateFormat,
          onRefreshRates = { viewModel.refreshGoldPrices() },
          onDeleteHolding = { viewModel.deleteGoldHolding(it) }
        )
      } else {
        // Savings Portfolio Tab
        SavingsPortfolioTab(
          goals = savingsGoals,
          language = language,
          onDepositClick = { selectedGoalForDeposit = it },
          onDeleteGoal = { viewModel.deleteSavingsGoal(it) }
        )
      }
    }

    FloatingActionButton(
      onClick = {
        if (selectedSubTab == 0) showAddGoldDialog = true
        else showAddSavingsGoalDialog = true
      },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("wallets_fab"),
      containerColor = if (selectedSubTab == 0) GoldAccent else MaterialTheme.colorScheme.primary,
      contentColor = Color.White
    ) {
      Icon(Icons.Default.Add, contentDescription = null)
    }
  }

  if (showAddGoldDialog) {
    AddGoldHoldingDialog(
      language = language,
      defaultPrice24k = goldRates.price24kPerGram,
      onDismiss = { showAddGoldDialog = false },
      onConfirm = { type, karat, weight, price, date, notes ->
        viewModel.addGoldHolding(type, karat, weight, price, date, notes)
        showAddGoldDialog = false
      }
    )
  }

  if (showAddSavingsGoalDialog) {
    AddSavingsGoalDialog(
      language = language,
      onDismiss = { showAddSavingsGoalDialog = false },
      onConfirm = { title, target, current, cat ->
        val deadline = System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000)
        viewModel.addSavingsGoal(title, target, current, deadline, cat)
        showAddSavingsGoalDialog = false
      }
    )
  }

  selectedGoalForDeposit?.let { goal ->
    DepositDialog(
      goal = goal,
      language = language,
      onDismiss = { selectedGoalForDeposit = null },
      onConfirm = { amt ->
        viewModel.depositToSavingsGoal(goal, amt)
        selectedGoalForDeposit = null
      }
    )
  }
}

@Composable
private fun GoldPortfolioTab(
  holdings: List<GoldHoldingEntity>,
  rates: com.example.data.remote.GoldRates,
  overview: com.example.ui.viewmodel.FinancialOverview,
  language: AppLanguage,
  dateFormat: SimpleDateFormat,
  onRefreshRates: () -> Unit,
  onDeleteHolding: (GoldHoldingEntity) -> Unit
) {
  val isProfit = overview.totalGoldProfitLoss >= 0

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item { Spacer(modifier = Modifier.height(10.dp)) }

    // Live Gold Rates Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = AppStrings.get("live_gold_price", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${AppStrings.get("last_updated", language)}: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(rates.lastUpdatedTimestamp))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            IconButton(onClick = onRefreshRates) {
              Icon(Icons.Default.Refresh, contentDescription = AppStrings.get("refresh_prices", language), tint = GoldAccent)
            }
          }

          // Karat Prices Grid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            GoldPricePill(title = "24K", price = rates.price24kPerGram, currency = AppStrings.get("currency", language), isMain = true, modifier = Modifier.weight(1f))
            GoldPricePill(title = "22K", price = rates.price22kPerGram, currency = AppStrings.get("currency", language), isMain = false, modifier = Modifier.weight(1f))
            GoldPricePill(title = "21K", price = rates.price21kPerGram, currency = AppStrings.get("currency", language), isMain = false, modifier = Modifier.weight(1f))
            GoldPricePill(title = "18K", price = rates.price18kPerGram, currency = AppStrings.get("currency", language), isMain = false, modifier = Modifier.weight(1f))
          }
        }
      }
    }

    // Gold Portfolio Value Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Box(
          modifier = Modifier
            .background(
              Brush.linearGradient(
                colors = listOf(Color(0xFF78350F), Color(0xFFB45309), Color(0xFFD97706))
              )
            )
            .padding(20.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = AppStrings.get("current_gold_market_value", language),
                  style = MaterialTheme.typography.labelMedium,
                  color = Color(0xFFFEF3C7)
                )
                Text(
                  text = "${String.format("%,.0f", overview.totalGoldCurrentValue)} ${AppStrings.get("currency", language)}",
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              }

              Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(46.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Gold24k)
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = AppStrings.get("total_gold_weight", language),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFFEF3C7)
                )
                Text(
                  text = "${overview.totalGoldWeightGrams} ${AppStrings.get("grams", language)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = AppStrings.get("gold_profit_loss", language),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFFEF3C7)
                )
                Text(
                  text = "${if (isProfit) "+" else ""}${String.format("%,.0f", overview.totalGoldProfitLoss)} ${AppStrings.get("currency", language)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (isProfit) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                )
              }
            }
          }
        }
      }
    }

    // Holdings List
    item {
      Text(
        text = if (language == AppLanguage.ARABIC) "المقتنيات والسبائك المسجلة" else "Gold Holdings & Bars",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    if (holdings.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(AppStrings.get("no_gold_items", language), color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    } else {
      items(holdings, key = { it.id }) { item ->
        GoldHoldingItemCard(
          holding = item,
          rates = rates,
          language = language,
          dateFormat = dateFormat,
          onDelete = { onDeleteHolding(item) }
        )
      }
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}

@Composable
private fun GoldPricePill(
  title: String,
  price: Double,
  currency: String,
  isMain: Boolean,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = if (isMain) GoldAccent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isMain) GoldAccent else MaterialTheme.colorScheme.onSurface)
      Text(
        text = "${String.format("%.1f", price)}",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(text = currency, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
fun GoldHoldingItemCard(
  holding: GoldHoldingEntity,
  rates: com.example.data.remote.GoldRates,
  language: AppLanguage,
  dateFormat: SimpleDateFormat,
  onDelete: () -> Unit
) {
  val currentPricePerGram = when (holding.karat) {
    24 -> rates.price24kPerGram
    22 -> rates.price22kPerGram
    21 -> rates.price21kPerGram
    18 -> rates.price18kPerGram
    else -> rates.price24kPerGram
  }
  val currentValue = holding.weightGrams * currentPricePerGram
  val purchaseCost = holding.weightGrams * holding.purchasePricePerGram
  val profit = currentValue - purchaseCost

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Surface(
          shape = CircleShape,
          color = GoldAccent.copy(alpha = 0.15f),
          modifier = Modifier.size(40.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = "${holding.karat}K",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = GoldDark
            )
          }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "${holding.weightGrams} ${AppStrings.get("grams", language)} • ${holding.notes.ifBlank { "عيار ${holding.karat}" }}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "شراء: ${String.format("%.0f", holding.purchasePricePerGram)} ${AppStrings.get("currency", language)}/جرام • ${dateFormat.format(Date(holding.purchaseDate))}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "${String.format("%,.0f", currentValue)} ${AppStrings.get("currency", language)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${if (profit >= 0) "+" else ""}${String.format("%,.0f", profit)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (profit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
          )
        }
        IconButton(onClick = onDelete) {
          Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

@Composable
private fun SavingsPortfolioTab(
  goals: List<SavingsGoalEntity>,
  language: AppLanguage,
  onDepositClick: (SavingsGoalEntity) -> Unit,
  onDeleteGoal: (SavingsGoalEntity) -> Unit
) {
  val totalSavings = goals.sumOf { it.currentAmount }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item { Spacer(modifier = Modifier.height(10.dp)) }

    // Total Savings Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Box(
          modifier = Modifier
            .background(
              Brush.linearGradient(
                colors = listOf(Color(0xFF065F46), Color(0xFF047857), Color(0xFF10B981))
              )
            )
            .padding(20.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = AppStrings.get("total_savings_accumulated", language),
              style = MaterialTheme.typography.labelMedium,
              color = Color(0xFFD1FAE5)
            )
            Text(
              text = "${String.format("%,.0f", totalSavings)} ${AppStrings.get("currency", language)}",
              style = MaterialTheme.typography.headlineLarge,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
          }
        }
      }
    }

    item {
      Text(
        text = AppStrings.get("savings_goals", language),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    items(goals, key = { it.id }) { goal ->
      SavingsGoalCard(
        goal = goal,
        language = language,
        onDeposit = { onDepositClick(goal) },
        onDelete = { onDeleteGoal(goal) }
      )
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}

@Composable
fun SavingsGoalCard(
  goal: SavingsGoalEntity,
  language: AppLanguage,
  onDeposit: () -> Unit,
  onDelete: () -> Unit
) {
  val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(
            text = "${AppStrings.get("current_saved", language)}: ${String.format("%,.0f", goal.currentAmount)} / ${String.format("%,.0f", goal.targetAmount)} ${AppStrings.get("currency", language)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        }
      }

      HorizontalProgressBar(
        progress = progress,
        color = Color(0xFF10B981),
        height = 8.dp
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "${(progress * 100).toInt()}% مكتمل", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        FilledTonalButton(onClick = onDeposit) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(AppStrings.get("add_deposit", language), fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
fun AddSavingsGoalDialog(
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (title: String, target: Double, current: Double, category: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var targetStr by remember { mutableStateOf("") }
  var currentStr by remember { mutableStateOf("0") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(AppStrings.get("new_savings_goal", language), fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(AppStrings.get("goal_title", language)) },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = targetStr,
          onValueChange = { targetStr = it },
          label = { Text(AppStrings.get("target_amount", language)) },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = currentStr,
          onValueChange = { currentStr = it },
          label = { Text(AppStrings.get("current_saved", language)) },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(onClick = {
        val tgt = targetStr.toDoubleOrNull() ?: 0.0
        val cur = currentStr.toDoubleOrNull() ?: 0.0
        if (title.isNotBlank() && tgt > 0) {
          onConfirm(title, tgt, cur, "GENERAL")
        }
      }) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(AppStrings.get("cancel", language)) }
    }
  )
}

@Composable
fun DepositDialog(
  goal: SavingsGoalEntity,
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (Double) -> Unit
) {
  var amountStr by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("${AppStrings.get("add_deposit", language)} - ${goal.title}", fontWeight = FontWeight.Bold) },
    text = {
      Column {
        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = it },
          label = { Text(AppStrings.get("deposit_amount", language) + " (${AppStrings.get("currency", language)})") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(onClick = {
        val amt = amountStr.toDoubleOrNull() ?: 0.0
        if (amt > 0) onConfirm(amt)
      }) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text(AppStrings.get("cancel", language)) }
    }
  )
}
