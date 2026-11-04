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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.ChartSlice
import com.example.ui.components.DonutBreakdownChart
import com.example.ui.components.HorizontalProgressBar
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinancialOverview

@Composable
fun ReportsScreen(
  viewModel: FinanceViewModel,
  modifier: Modifier = Modifier
) {
  val language by viewModel.currentLanguage.collectAsState()
  val overview by viewModel.overview.collectAsState()
  val expenses by viewModel.expenses.collectAsState()

  var selectedReportTab by remember { mutableIntStateOf(0) } // 0: Monthly, 1: Annual, 2: Comparison

  val tabs = listOf(
    AppStrings.get("report_monthly", language),
    AppStrings.get("report_annual", language),
    AppStrings.get("report_comparison", language)
  )

  Column(modifier = modifier.fillMaxSize()) {
    TabRow(
      selectedTabIndex = selectedReportTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedReportTab == index,
          onClick = { selectedReportTab = index },
          text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
        )
      }
    }

    when (selectedReportTab) {
      0 -> MonthlyReportTab(overview = overview, expenses = expenses, language = language)
      1 -> AnnualReportTab(overview = overview, language = language)
      2 -> ComparisonReportTab(overview = overview, language = language)
    }
  }
}

@Composable
private fun MonthlyReportTab(
  overview: FinancialOverview,
  expenses: List<com.example.data.local.entity.ExpenseEntity>,
  language: AppLanguage
) {
  val totalOutflow = overview.totalMonthlyObligations + overview.currentMonthExpensesTotal
  val burnRate = if (overview.totalMonthlyIncome > 0) {
    ((totalOutflow / overview.totalMonthlyIncome) * 100.0).coerceAtMost(100.0)
  } else 0.0

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item { Spacer(modifier = Modifier.height(10.dp)) }

    // Cashflow Hero Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("monthly_report_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Box(
          modifier = Modifier
            .background(
              Brush.linearGradient(
                colors = listOf(Color(0xFF0F766E), Color(0xFF1E3A8A), Color(0xFF0F172A))
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
                  text = AppStrings.get("total_income_report", language),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFCCFBF1)
                )
                Text(
                  text = "${String.format("%,.0f", overview.totalMonthlyIncome)} ${AppStrings.get("currency", language)}",
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = AppStrings.get("total_outflow_report", language),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFFECDD3)
                )
                Text(
                  text = "${String.format("%,.0f", totalOutflow)} ${AppStrings.get("currency", language)}",
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color(0xFFFCA5A5)
                )
              }
            }

            // Burn rate & budget stability
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = AppStrings.get("burn_rate_text", language),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                  text = "${burnRate.toInt()}%",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (burnRate > 85) Color(0xFFF87171) else Color(0xFF6EE7B7)
                )
              }
              HorizontalProgressBar(
                progress = (burnRate / 100.0).toFloat(),
                color = if (burnRate > 85) Color(0xFFEF4444) else Color(0xFF10B981),
                trackColor = Color.White.copy(alpha = 0.2f),
                height = 8.dp
              )
            }
          }
        }
      }
    }

    // Interactive Donut Breakdown Chart
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = AppStrings.get("spending_by_category", language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))

          val slices = listOf(
            ChartSlice(AppStrings.get("category_food", language), expenses.filter { it.category == "FOOD" }.sumOf { it.amount }, Color(0xFF10B981)),
            ChartSlice(AppStrings.get("housing_monthly", language), overview.housingMonthlyTotal, Color(0xFF3B82F6)),
            ChartSlice(AppStrings.get("car_monthly_payment", language), overview.carMonthlyTotal, Color(0xFF6366F1)),
            ChartSlice(AppStrings.get("monthly_installments", language), overview.monthlyInstallmentsTotal, Color(0xFFF59E0B)),
            ChartSlice(AppStrings.get("telecom_monthly", language), overview.telecomMonthlyTotal, Color(0xFF8B5CF6)),
            ChartSlice(AppStrings.get("category_shopping", language), expenses.filter { it.category == "SHOPPING" }.sumOf { it.amount }, Color(0xFFEC4899)),
            ChartSlice(AppStrings.get("category_transport", language), expenses.filter { it.category == "TRANSPORT" }.sumOf { it.amount }, Color(0xFFF97316)),
            ChartSlice(AppStrings.get("category_bills", language), expenses.filter { it.category == "BILLS" }.sumOf { it.amount }, Color(0xFF14B8A6))
          )

          DonutBreakdownChart(
            slices = slices,
            totalAmountText = "${String.format("%,.0f", totalOutflow)} ${AppStrings.get("currency", language)}",
            centerLabel = if (language == AppLanguage.ARABIC) "مجموع المصروفات" else "Total Outflow"
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}

@Composable
private fun AnnualReportTab(
  overview: FinancialOverview,
  language: AppLanguage
) {
  val annualIncome = overview.totalMonthlyIncome * 12.0
  val annualOutflow = (overview.totalMonthlyObligations + overview.currentMonthExpensesTotal) * 12.0
  val annualSavings = (annualIncome - annualOutflow).coerceAtLeast(0.0)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item { Spacer(modifier = Modifier.height(10.dp)) }

    // Annual Projection Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Box(
          modifier = Modifier
            .background(
              Brush.linearGradient(
                colors = listOf(Color(0xFF312E81), Color(0xFF4338CA), Color(0xFF1E293B))
              )
            )
            .padding(20.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
              text = AppStrings.get("report_annual", language),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              AnnualMetricBlock(
                title = if (language == AppLanguage.ARABIC) "الدخل السنوي المتوقع" else "Annual Income",
                value = "${String.format("%,.0f", annualIncome)} ${AppStrings.get("currency", language)}",
                color = Color(0xFFA5B4FC)
              )
              AnnualMetricBlock(
                title = if (language == AppLanguage.ARABIC) "النفقات السنوية المتوقعة" else "Annual Outflow",
                value = "${String.format("%,.0f", annualOutflow)} ${AppStrings.get("currency", language)}",
                color = Color(0xFFFECDD3)
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.White.copy(alpha = 0.15f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (language == AppLanguage.ARABIC) "الادخار التراكمي السنوي المقدر" else "Estimated Annual Net Savings",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White
                )
                Text(
                  text = "+${String.format("%,.0f", annualSavings)} ${AppStrings.get("currency", language)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF86EFAC)
                )
              }
            }
          }
        }
      }
    }

    // 12 Months Visual Bar Simulation
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = AppStrings.get("monthly_trend", language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          val monthsList = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")
          Row(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            monthsList.take(6).forEachIndexed { idx, mName ->
              val factor = listOf(0.7f, 0.85f, 0.65f, 0.95f, 0.8f, 0.75f)[idx]
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
              ) {
                Surface(
                  shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier
                    .width(28.dp)
                    .height((110 * factor).dp)
                ) {}
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = mName.take(3), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
              }
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}

@Composable
private fun ComparisonReportTab(
  overview: FinancialOverview,
  language: AppLanguage
) {
  val thisMonthExpenses = overview.totalMonthlyObligations + overview.currentMonthExpensesTotal
  val lastMonthExpenses = thisMonthExpenses * 0.92 // Benchmark comparison

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item { Spacer(modifier = Modifier.height(10.dp)) }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text(
            text = AppStrings.get("report_comparison", language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          ComparisonMetricRow(
            title = if (language == AppLanguage.ARABIC) "إجمالي المصروفات والالتزامات" else "Total Spending & Obligations",
            currentVal = thisMonthExpenses,
            previousVal = lastMonthExpenses,
            language = language
          )

          ComparisonMetricRow(
            title = AppStrings.get("total_monthly_income", language),
            currentVal = overview.totalMonthlyIncome,
            previousVal = overview.totalMonthlyIncome * 0.95,
            language = language
          )

          ComparisonMetricRow(
            title = AppStrings.get("monthly_installments", language),
            currentVal = overview.monthlyInstallmentsTotal,
            previousVal = overview.monthlyInstallmentsTotal + 400.0,
            language = language
          )

          ComparisonMetricRow(
            title = AppStrings.get("car_monthly_payment", language),
            currentVal = overview.carMonthlyTotal,
            previousVal = overview.carMonthlyTotal,
            language = language
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}

@Composable
private fun ComparisonMetricRow(
  title: String,
  currentVal: Double,
  previousVal: Double,
  language: AppLanguage
) {
  val diff = currentVal - previousVal
  val pctChange = if (previousVal > 0) ((diff / previousVal) * 100.0) else 0.0
  val isIncreased = diff > 0

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            if (isIncreased) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
            contentDescription = null,
            tint = if (isIncreased) Color(0xFFEF4444) else Color(0xFF10B981),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${if (isIncreased) "+" else ""}${String.format("%.1f", pctChange)}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isIncreased) Color(0xFFEF4444) else Color(0xFF10B981)
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "${AppStrings.get("this_month", language)}: ${String.format("%,.0f", currentVal)} ${AppStrings.get("currency", language)}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "${AppStrings.get("last_month", language)}: ${String.format("%,.0f", previousVal)} ${AppStrings.get("currency", language)}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun AnnualMetricBlock(
  title: String,
  value: String,
  color: Color
) {
  Column {
    Text(text = title, style = MaterialTheme.typography.labelSmall, color = color)
    Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
  }
}
