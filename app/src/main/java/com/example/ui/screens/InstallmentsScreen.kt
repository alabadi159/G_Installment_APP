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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.InstallmentPlanEntity
import com.example.ui.components.HorizontalProgressBar
import com.example.ui.dialogs.AddInstallmentDialog
import com.example.ui.dialogs.EditInstallmentDialog
import com.example.ui.dialogs.formatMonthYear
import com.example.ui.dialogs.getMonthFromTimestamp
import com.example.ui.dialogs.getYearFromTimestamp
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.SnbGreen
import com.example.ui.theme.TabbyGreen
import com.example.ui.theme.TamaraOrange
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InstallmentsScreen(
  viewModel: FinanceViewModel,
  modifier: Modifier = Modifier
) {
  val language by viewModel.currentLanguage.collectAsState()
  val installments by viewModel.installments.collectAsState()
  var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Active, 2: Completed, 3: New
  var showAddDialog by remember { mutableStateOf(false) }
  var selectedInstallmentForEdit by remember { mutableStateOf<InstallmentPlanEntity?>(null) }

  val tabs = listOf(
    AppStrings.get("all_tab", language),
    AppStrings.get("active_tab", language),
    AppStrings.get("completed_tab", language),
    AppStrings.get("new_tab", language)
  )

  val filteredInstallments = when (selectedTab) {
    1 -> installments.filter { it.status == "ACTIVE" }
    2 -> installments.filter { it.status == "COMPLETED" }
    3 -> installments.filter { it.status == "NEW" }
    else -> installments
  }

  val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale(language.code))

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      androidx.compose.material3.TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
          )
        }
      }

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // Provider Summary Bar
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            ProviderSummaryBadge(
              providerName = "Tabby",
              badgeColor = TabbyGreen,
              count = installments.count { it.provider == "TABBY" && it.status == "ACTIVE" },
              modifier = Modifier.weight(1f)
            )
            ProviderSummaryBadge(
              providerName = "Tamara",
              badgeColor = TamaraOrange,
              count = installments.count { it.provider == "TAMARA" && it.status == "ACTIVE" },
              modifier = Modifier.weight(1f)
            )
            ProviderSummaryBadge(
              providerName = "SNB FLEX (4M)",
              badgeColor = SnbGreen,
              count = installments.count { it.provider == "SNB_FLEX" && it.status == "ACTIVE" },
              modifier = Modifier.weight(1f)
            )
          }
        }

        if (filteredInstallments.isEmpty()) {
          item {
            Card(
              modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = if (language == AppLanguage.ARABIC) "لا توجد أقساط في هذا القسم" else "No installments found in this tab",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        } else {
          items(filteredInstallments, key = { it.id }) { installment ->
            InstallmentCard(
              installment = installment,
              language = language,
              dateFormat = dateFormat,
              onMarkPaid = { viewModel.markInstallmentMonthPaid(installment) },
              onEdit = { selectedInstallmentForEdit = installment },
              onDelete = { viewModel.deleteInstallment(installment) }
            )
          }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
      }
    }

    FloatingActionButton(
      onClick = { showAddDialog = true },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("installments_fab"),
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
      Icon(Icons.Default.Add, contentDescription = AppStrings.get("add_installment", language))
    }
  }

  if (showAddDialog) {
    AddInstallmentDialog(
      language = language,
      onDismiss = { showAddDialog = false },
      onConfirm = { t, p, tot, m, s, n, paid ->
        viewModel.addInstallment(t, p, tot, m, s, n, paid)
        showAddDialog = false
      }
    )
  }

  if (selectedInstallmentForEdit != null) {
    EditInstallmentDialog(
      installment = selectedInstallmentForEdit!!,
      language = language,
      onDismiss = { selectedInstallmentForEdit = null },
      onConfirm = { id, title, provider, totalAmount, monthsCount, startDate, paidMonthsCount, notes ->
        viewModel.updateInstallment(id, title, provider, totalAmount, monthsCount, startDate, paidMonthsCount, notes)
        selectedInstallmentForEdit = null
      }
    )
  }
}

@Composable
private fun ProviderSummaryBadge(
  providerName: String,
  badgeColor: Color,
  count: Int,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.15f))
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = providerName,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = badgeColor
      )
      Text(
        text = "$count",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
fun InstallmentCard(
  installment: InstallmentPlanEntity,
  language: AppLanguage,
  dateFormat: SimpleDateFormat,
  onMarkPaid: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val providerColor = when (installment.provider) {
    "TABBY" -> TabbyGreen
    "TAMARA" -> TamaraOrange
    "SNB_FLEX" -> SnbGreen
    else -> Color(0xFF6366F1)
  }

  val providerTitle = when (installment.provider) {
    "TABBY" -> "تابي Tabby"
    "TAMARA" -> "تمارا Tamara"
    "SNB_FLEX" -> "الأهلي فليكس SNB FLEX"
    else -> AppStrings.get("other_provider", language)
  }

  val progress = if (installment.monthsCount > 0) {
    installment.paidMonthsCount.toFloat() / installment.monthsCount.toFloat()
  } else 0f

  val isCompleted = installment.paidMonthsCount >= installment.monthsCount || installment.status == "COMPLETED"

  val startMonth = getMonthFromTimestamp(installment.startDate)
  val startYear = getYearFromTimestamp(installment.startDate)
  val endCal = java.util.Calendar.getInstance().apply {
    timeInMillis = installment.startDate
    add(java.util.Calendar.MONTH, installment.monthsCount)
  }
  val endMonth = endCal.get(java.util.Calendar.MONTH) + 1
  val endYear = endCal.get(java.util.Calendar.YEAR)
  val remainingMonths = (installment.monthsCount - installment.paidMonthsCount).coerceAtLeast(0)

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      // Header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = providerColor.copy(alpha = 0.2f),
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Text(
              text = providerTitle,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = providerColor
            )
          }

          if (isCompleted) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF10B981).copy(alpha = 0.15f)
            ) {
              Text(
                text = AppStrings.get("status_completed", language),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF10B981),
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onEdit,
            modifier = Modifier.size(28.dp).testTag("edit_installment_${installment.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = AppStrings.get("edit_plan", language),
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(4.dp))
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp).testTag("delete_installment_${installment.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = AppStrings.get("delete", language),
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Title & Amounts
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = installment.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${AppStrings.get("start_date", language)}: ${formatMonthYear(startMonth, startYear, language)} ➔ ${formatMonthYear(endMonth, endYear, language)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "${AppStrings.get("date", language)}: ${dateFormat.format(Date(installment.startDate))}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "${String.format("%,.0f", installment.monthlyAmount)} ${AppStrings.get("currency", language)} / ${AppStrings.get("month", language)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "${AppStrings.get("total_order_amount", language)}: ${String.format("%,.0f", installment.totalAmount)} ${AppStrings.get("currency", language)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Progress bar
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "${AppStrings.get("paid_installments", language)}: ${installment.paidMonthsCount} / ${installment.monthsCount}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = providerColor
          )
        }

        HorizontalProgressBar(
          progress = progress,
          color = providerColor,
          height = 10.dp
        )
      }

      // Schedule pills representation (Month 1, Month 2, Month 3, ...)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        for (i in 1..installment.monthsCount) {
          val isPaid = i <= installment.paidMonthsCount
          Box(
            modifier = Modifier
              .weight(1f)
              .height(28.dp)
              .background(
                color = if (isPaid) providerColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "$i",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (isPaid) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          }
        }
      }

      // Action Row
      if (!isCompleted) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          FilledTonalButton(
            onClick = onMarkPaid,
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          ) {
            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(AppStrings.get("mark_month_paid", language), fontSize = 12.sp)
          }
        }
      }
    }
  }
}
