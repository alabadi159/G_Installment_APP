package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ExpenseEntity
import com.example.ui.components.ChartSlice
import com.example.ui.components.DonutBreakdownChart
import com.example.ui.dialogs.AddExpenseDialog
import com.example.ui.dialogs.AddInstallmentDialog
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.Gold24k
import com.example.ui.theme.TabbyGreen
import com.example.ui.theme.TamaraOrange
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  viewModel: FinanceViewModel,
  onNavigateToIncome: () -> Unit,
  onNavigateToInstallments: () -> Unit,
  onNavigateToCar: () -> Unit,
  onNavigateToWallets: () -> Unit,
  onNavigateToCommitments: () -> Unit,
  onNavigateToReports: () -> Unit,
  modifier: Modifier = Modifier
) {
  val language by viewModel.currentLanguage.collectAsState()
  val overview by viewModel.overview.collectAsState()
  val expenses by viewModel.expenses.collectAsState()
  val installments by viewModel.installments.collectAsState()
  val carLoan by viewModel.carLoan.collectAsState()
  val carServices by viewModel.carServices.collectAsState()
  val housing by viewModel.housing.collectAsState()
  val alerts by viewModel.alerts.collectAsState()

  var showAddExpenseDialog by remember { mutableStateOf(false) }
  var showAddIncomeDialog by remember { mutableStateOf(false) }
  var showAddInstallmentDialog by remember { mutableStateOf(false) }

  val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale(language.code))
  val isSurplus = overview.netMonthlyCashflow >= 0

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item { Spacer(modifier = Modifier.height(4.dp)) }

      // Hero Banner: Financial Health, Income & Monthly Obligations
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("summary_card"),
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
          Box(
            modifier = Modifier
              .background(
                Brush.linearGradient(
                  colors = listOf(
                    Color(0xFF0F766E),
                    Color(0xFF134E4A),
                    Color(0xFF1E293B)
                  )
                )
              )
              .padding(20.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = AppStrings.get("total_monthly_income", language),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFCCFBF1)
                  )
                  Text(
                    text = "${String.format("%,.0f", overview.totalMonthlyIncome)} ${AppStrings.get("currency", language)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6EE7B7)
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = AppStrings.get("total_monthly_obligations", language),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFECDD3)
                  )
                  Text(
                    text = "${String.format("%,.0f", overview.totalMonthlyObligations)} ${AppStrings.get("currency", language)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                }
              }

              // Net Surplus / Deficit Row
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = AppStrings.get("net_cashflow", language),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                  )
                  Text(
                    text = "${if (isSurplus) "+" else ""}${String.format("%,.0f", overview.netMonthlyCashflow)} ${AppStrings.get("currency", language)} (${overview.savingsRatePercentage}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSurplus) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                  )
                }
              }

              // Sub-metrics row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                MetricMiniItem(
                  title = AppStrings.get("daily_expenses_this_month", language),
                  value = "${String.format("%,.0f", overview.currentMonthExpensesTotal)} ${AppStrings.get("currency", language)}",
                  icon = Icons.Default.ShoppingBag,
                  accentColor = Color(0xFFFDE68A)
                )

                MetricMiniItem(
                  title = AppStrings.get("savings_and_gold_value", language),
                  value = "${String.format("%,.0f", overview.totalSavingsGoldValue)} ${AppStrings.get("currency", language)}",
                  icon = Icons.Default.Savings,
                  accentColor = Gold24k
                )
              }
            }
          }
        }
      }

      // Quick Action Buttons
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = AppStrings.get("quick_actions", language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            QuickActionButton(
              title = AppStrings.get("add_income", language),
              icon = Icons.Default.MonetizationOn,
              color = Color(0xFF059669),
              onClick = { showAddIncomeDialog = true },
              modifier = Modifier.weight(1f).testTag("quick_add_income")
            )
            QuickActionButton(
              title = AppStrings.get("add_expense", language),
              icon = Icons.Default.ShoppingBag,
              color = Color(0xFF0F766E),
              onClick = { showAddExpenseDialog = true },
              modifier = Modifier.weight(1f).testTag("quick_add_expense")
            )
            QuickActionButton(
              title = AppStrings.get("add_installment", language),
              icon = Icons.Default.CreditCard,
              color = Color(0xFFD97706),
              onClick = { showAddInstallmentDialog = true },
              modifier = Modifier.weight(1f).testTag("quick_add_installment")
            )
            QuickActionButton(
              title = AppStrings.get("reports", language),
              icon = Icons.Default.BarChart,
              color = Color(0xFF3B82F6),
              onClick = onNavigateToReports,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // Interactive Expenses & Obligations Breakdown Chart
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = AppStrings.get("expense_breakdown", language),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val slices = listOf(
              ChartSlice(
                label = AppStrings.get("monthly_installments", language),
                value = overview.monthlyInstallmentsTotal,
                color = Color(0xFFF59E0B)
              ),
              ChartSlice(
                label = AppStrings.get("car_monthly_payment", language),
                value = overview.carMonthlyTotal,
                color = Color(0xFF3B82F6)
              ),
              ChartSlice(
                label = AppStrings.get("housing_monthly", language),
                value = overview.housingMonthlyTotal,
                color = Color(0xFF10B981)
              ),
              ChartSlice(
                label = AppStrings.get("telecom_monthly", language),
                value = overview.telecomMonthlyTotal,
                color = Color(0xFF8B5CF6)
              ),
              ChartSlice(
                label = AppStrings.get("daily_expenses_this_month", language),
                value = overview.currentMonthExpensesTotal,
                color = Color(0xFFEC4899)
              )
            )

            DonutBreakdownChart(
              slices = slices,
              totalAmountText = "${String.format("%,.0f", overview.totalMonthlyObligations + overview.currentMonthExpensesTotal)} ${AppStrings.get("currency", language)}",
              centerLabel = if (language == AppLanguage.ARABIC) "إجمالي المصروفات" else "Total Monthly"
            )
          }
        }
      }

      // High-Priority Upcoming Dues Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Text(
                text = AppStrings.get("upcoming_dues", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            // Dues list
            UpcomingDueRow(
              title = if (language == AppLanguage.ARABIC) "إيجار المسكن الشهري" else "Housing Monthly Rent",
              date = housing?.nextDueDate?.let { dateFormat.format(Date(it)) } ?: "قريباً",
              amount = "${housing?.rentAmount ?: 3500.0} ${AppStrings.get("currency", language)}",
              icon = Icons.Default.Payments,
              badgeColor = Color(0xFF10B981)
            )

            carLoan?.let { car ->
              UpcomingDueRow(
                title = "${AppStrings.get("car_monthly_payment", language)} (${car.carName})",
                date = "28 من كل شهر",
                amount = "${car.monthlyInstallment} ${AppStrings.get("currency", language)}",
                icon = Icons.Default.DirectionsCar,
                badgeColor = Color(0xFF3B82F6)
              )
            }

            installments.firstOrNull { it.status == "ACTIVE" }?.let { inst ->
              UpcomingDueRow(
                title = "${inst.title} (${inst.provider})",
                date = "قسط ${inst.paidMonthsCount + 1} من ${inst.monthsCount}",
                amount = "${inst.monthlyAmount} ${AppStrings.get("currency", language)}",
                icon = Icons.Default.CreditCard,
                badgeColor = if (inst.provider == "TABBY") TabbyGreen else if (inst.provider == "TAMARA") TamaraOrange else Color(0xFF007A3D)
              )
            }
          }
        }
      }

      // Recent Expenses Section
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = AppStrings.get("recent_expenses", language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          FilledTonalButton(onClick = onNavigateToCommitments) {
            Text(AppStrings.get("view_all", language), fontSize = 12.sp)
          }
        }
      }

      if (expenses.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Box(
              modifier = Modifier.fillMaxWidth().padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = AppStrings.get("no_expenses_recorded", language),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(expenses.take(5)) { expense ->
          ExpenseItemCard(
            expense = expense,
            language = language,
            dateFormat = dateFormat,
            onDelete = { viewModel.deleteExpense(expense) }
          )
        }
      }

      item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // Floating Action Button
    FloatingActionButton(
      onClick = { showAddExpenseDialog = true },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("dashboard_fab"),
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
      Icon(Icons.Default.Add, contentDescription = AppStrings.get("add_expense", language))
    }
  }

  if (showAddExpenseDialog) {
    AddExpenseDialog(
      language = language,
      onDismiss = { showAddExpenseDialog = false },
      onConfirm = { t, a, c, m, n ->
        viewModel.addExpense(t, a, c, m, n)
        showAddExpenseDialog = false
      }
    )
  }

  if (showAddIncomeDialog) {
    com.example.ui.dialogs.AddIncomeDialog(
      language = language,
      onDismiss = { showAddIncomeDialog = false },
      onConfirm = { t, a, c, date, rec, n ->
        viewModel.addIncome(t, a, c, date, rec, n)
        showAddIncomeDialog = false
      }
    )
  }

  if (showAddInstallmentDialog) {
    AddInstallmentDialog(
      language = language,
      onDismiss = { showAddInstallmentDialog = false },
      onConfirm = { t, p, tot, m, s, n, paid ->
        viewModel.addInstallment(t, p, tot, m, s, n, paid)
        showAddInstallmentDialog = false
      }
    )
  }
}

@Composable
private fun MetricMiniItem(
  title: String,
  value: String,
  icon: ImageVector,
  accentColor: Color
) {
  Column {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color(0xFFCCFBF1), fontSize = 11.sp)
    }
    Text(
      text = value,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = Color.White
    )
  }
}

@Composable
private fun QuickActionButton(
  title: String,
  icon: ImageVector,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.size(36.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun UpcomingDueRow(
  title: String,
  date: String,
  amount: String,
  icon: ImageVector,
  badgeColor: Color
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Surface(
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = badgeColor.copy(alpha = 0.2f)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
        }
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(text = date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
    Text(
      text = amount,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun ExpenseItemCard(
  expense: ExpenseEntity,
  language: AppLanguage,
  dateFormat: SimpleDateFormat,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
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
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
          modifier = Modifier.size(40.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.ShoppingBag,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = expense.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "${AppStrings.get("category_" + expense.category.lowercase(), language)} • ${dateFormat.format(Date(expense.date))}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "${String.format("%,.2f", expense.amount)} ${AppStrings.get("currency", language)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onDelete) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = AppStrings.get("delete", language),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}
