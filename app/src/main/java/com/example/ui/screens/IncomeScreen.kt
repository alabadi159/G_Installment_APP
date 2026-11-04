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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.IncomeEntity
import com.example.ui.dialogs.AddIncomeDialog
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IncomeScreen(
  viewModel: FinanceViewModel,
  modifier: Modifier = Modifier
) {
  val language by viewModel.currentLanguage.collectAsState()
  val overview by viewModel.overview.collectAsState()
  val incomes by viewModel.incomes.collectAsState()

  var showAddDialog by remember { mutableStateOf(false) }
  val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale(language.code))

  val isSurplus = overview.netMonthlyCashflow >= 0

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item { Spacer(modifier = Modifier.height(10.dp)) }

      // Income Hero Banner & Net Cashflow
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("income_hero_card"),
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
          Box(
            modifier = Modifier
              .background(
                Brush.linearGradient(
                  colors = listOf(
                    Color(0xFF047857),
                    Color(0xFF065F46),
                    Color(0xFF0F172A)
                  )
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
                    text = AppStrings.get("total_monthly_income", language),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFD1FAE5)
                  )
                  Text(
                    text = "${String.format("%,.0f", overview.totalMonthlyIncome)} ${AppStrings.get("currency", language)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                  )
                }

                Surface(
                  shape = CircleShape,
                  color = Color.White.copy(alpha = 0.2f),
                  modifier = Modifier.size(48.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.MonetizationOn,
                      contentDescription = null,
                      tint = Color(0xFF6EE7B7)
                    )
                  }
                }
              }

              // Income vs Outflow Side-by-Side Summary
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(14.dp).fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        if (isSurplus) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isSurplus) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = if (isSurplus) AppStrings.get("net_surplus", language) else AppStrings.get("net_deficit", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                      )
                    }
                    Text(
                      text = "${String.format("%,.0f", overview.netMonthlyCashflow)} ${AppStrings.get("currency", language)}",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (isSurplus) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                    )
                  }

                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = AppStrings.get("savings_rate", language),
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                      text = "${overview.savingsRatePercentage}%",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                }
              }

              // All-time recorded total
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = AppStrings.get("total_income_all_time", language),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFA7F3D0)
                )
                Text(
                  text = "${String.format("%,.0f", overview.totalAllTimeIncome)} ${AppStrings.get("currency", language)}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }
        }
      }

      // Income Sources List Header
      item {
        Text(
          text = AppStrings.get("income_sources", language),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      if (incomes.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Box(
              modifier = Modifier.padding(32.dp).fillMaxWidth(),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = AppStrings.get("no_incomes_recorded", language),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(incomes, key = { it.id }) { income ->
          IncomeItemCard(
            income = income,
            language = language,
            dateFormat = dateFormat,
            onDelete = { viewModel.deleteIncome(income) }
          )
        }
      }

      item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    FloatingActionButton(
      onClick = { showAddDialog = true },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("income_fab"),
      containerColor = Color(0xFF059669),
      contentColor = Color.White
    ) {
      Icon(Icons.Default.Add, contentDescription = AppStrings.get("add_income", language))
    }
  }

  if (showAddDialog) {
    AddIncomeDialog(
      language = language,
      onDismiss = { showAddDialog = false },
      onConfirm = { title, amt, cat, date, rec, notes ->
        viewModel.addIncome(title, amt, cat, date, rec, notes)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun IncomeItemCard(
  income: IncomeEntity,
  language: AppLanguage,
  dateFormat: SimpleDateFormat,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val (categoryLabel, icon, iconColor) = when (income.category) {
    "SALARY" -> Triple(AppStrings.get("category_salary", language), Icons.Default.AccountBalance, Color(0xFF10B981))
    "FREELANCE" -> Triple(AppStrings.get("category_freelance", language), Icons.Default.Laptop, Color(0xFF3B82F6))
    "INVESTMENTS" -> Triple(AppStrings.get("category_investments", language), Icons.Default.TrendingUp, Color(0xFFF59E0B))
    "RENTAL" -> Triple(AppStrings.get("category_rental", language), Icons.Default.HomeWork, Color(0xFF8B5CF6))
    "BONUS" -> Triple(AppStrings.get("category_bonus", language), Icons.Default.CardGiftcard, Color(0xFFEC4899))
    "BUSINESS" -> Triple(AppStrings.get("category_business", language), Icons.Default.BusinessCenter, Color(0xFF0F766E))
    else -> Triple(AppStrings.get("category_other_income", language), Icons.Default.MonetizationOn, Color(0xFF64748B))
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
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
          color = iconColor.copy(alpha = 0.15f),
          modifier = Modifier.size(42.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = iconColor,
              modifier = Modifier.size(22.dp)
            )
          }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = income.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "$categoryLabel • ${dateFormat.format(Date(income.receiptDate))}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          if (income.isRecurringMonthly) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFF10B981).copy(alpha = 0.12f),
              modifier = Modifier.padding(top = 4.dp)
            ) {
              Text(
                text = if (language == AppLanguage.ARABIC) "شهري متكرر" else "Monthly Recurring",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = Color(0xFF10B981),
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "+${String.format("%,.0f", income.amount)} ${AppStrings.get("currency", language)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF10B981)
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
