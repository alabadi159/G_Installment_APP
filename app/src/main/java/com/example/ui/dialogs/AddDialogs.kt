package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CarLoanEntity
import com.example.data.local.entity.HousingEntity
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.SnbGreen
import com.example.ui.theme.TabbyGreen
import com.example.ui.theme.TamaraOrange

@Composable
fun AddExpenseDialog(
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (title: String, amount: Double, category: String, isMonthly: Boolean, notes: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var amountStr by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("FOOD") }
  var isMonthly by remember { mutableStateOf(false) }
  var notes by remember { mutableStateOf("") }

  val categories = listOf("FOOD", "SHOPPING", "TRANSPORT", "BILLS", "HEALTH", "ENTERTAINMENT", "OTHER")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = AppStrings.get("add_expense", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(AppStrings.get("order_title", language)) },
          modifier = Modifier.fillMaxWidth().testTag("expense_title_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = it },
          label = { Text(AppStrings.get("amount", language) + " (${AppStrings.get("currency", language)})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
          singleLine = true
        )

        Text(
          text = AppStrings.get("category", language),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          categories.take(4).forEach { cat ->
            val labelKey = "category_" + cat.lowercase()
            FilterChip(
              selected = selectedCategory == cat,
              onClick = { selectedCategory = cat },
              label = { Text(AppStrings.get(labelKey, language), fontSize = 11.sp) }
            )
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          categories.drop(4).forEach { cat ->
            val labelKey = "category_" + cat.lowercase()
            FilterChip(
              selected = selectedCategory == cat,
              onClick = { selectedCategory = cat },
              label = { Text(AppStrings.get(labelKey, language), fontSize = 11.sp) }
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = if (language == AppLanguage.ARABIC) "مصروف شهري متكرر؟" else "Recurring Monthly?",
            style = MaterialTheme.typography.bodyMedium
          )
          Switch(checked = isMonthly, onCheckedChange = { isMonthly = it })
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(AppStrings.get("notes", language)) },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountStr.toDoubleOrNull() ?: 0.0
          if (title.isNotBlank() && amt > 0) {
            onConfirm(title, amt, selectedCategory, isMonthly, notes)
          }
        },
        modifier = Modifier.testTag("save_expense_button")
      ) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}

fun buildDateTimestamp(month: Int, year: Int, day: Int = 1): Long {
  val cal = java.util.Calendar.getInstance()
  cal.set(java.util.Calendar.YEAR, year)
  cal.set(java.util.Calendar.MONTH, (month - 1).coerceIn(0, 11))
  cal.set(java.util.Calendar.DAY_OF_MONTH, day)
  cal.set(java.util.Calendar.HOUR_OF_DAY, 12)
  cal.set(java.util.Calendar.MINUTE, 0)
  cal.set(java.util.Calendar.SECOND, 0)
  return cal.timeInMillis
}

fun getMonthFromTimestamp(timestamp: Long): Int {
  val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
  return cal.get(java.util.Calendar.MONTH) + 1
}

fun getYearFromTimestamp(timestamp: Long): Int {
  val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
  return cal.get(java.util.Calendar.YEAR)
}

fun formatMonthYear(month: Int, year: Int, language: AppLanguage): String {
  val monthNamesAr = listOf(
    "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
    "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
  )
  val monthNamesEn = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  )
  val mIdx = (month - 1).coerceIn(0, 11)
  return if (language == AppLanguage.ARABIC) "${monthNamesAr[mIdx]} $year" else "${monthNamesEn[mIdx]} $year"
}

@Composable
fun MonthYearSelector(
  selectedMonth: Int,
  selectedYear: Int,
  language: AppLanguage,
  label: String,
  onMonthChange: (Int) -> Unit,
  onYearChange: (Int) -> Unit,
  yearRange: List<Int> = listOf(2021, 2022, 2023, 2024, 2025, 2026, 2027, 2028)
) {
  val monthNamesAr = listOf(
    "1 (يناير)", "2 (فبراير)", "3 (مارس)", "4 (أبريل)", "5 (مايو)", "6 (يونيو)",
    "7 (يوليو)", "8 (أغسطس)", "9 (سبتمبر)", "10 (أكتوبر)", "11 (نوفمبر)", "12 (ديسمبر)"
  )
  val monthNamesEn = listOf(
    "1 (Jan)", "2 (Feb)", "3 (Mar)", "4 (Apr)", "5 (May)", "6 (Jun)",
    "7 (Jul)", "8 (Aug)", "9 (Sep)", "10 (Oct)", "11 (Nov)", "12 (Dec)"
  )
  val monthNames = if (language == AppLanguage.ARABIC) monthNamesAr else monthNamesEn

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
  ) {
    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = formatMonthYear(selectedMonth, selectedYear, language),
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      // Year selection chips
      Text(
        text = AppStrings.get("start_year", language),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        yearRange.take(4).forEach { y ->
          FilterChip(
            selected = selectedYear == y,
            onClick = { onYearChange(y) },
            label = { Text("$y", fontSize = 11.sp) },
            modifier = Modifier.weight(1f)
          )
        }
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        yearRange.drop(4).take(4).forEach { y ->
          FilterChip(
            selected = selectedYear == y,
            onClick = { onYearChange(y) },
            label = { Text("$y", fontSize = 11.sp) },
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Month selection chips
      Text(
        text = AppStrings.get("start_month", language),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(1..4, 5..8, 9..12).forEach { range ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            range.forEach { m ->
              FilterChip(
                selected = selectedMonth == m,
                onClick = { onMonthChange(m) },
                label = { Text(monthNames[m - 1], fontSize = 10.sp) },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun AddInstallmentDialog(
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (title: String, provider: String, totalAmount: Double, monthsCount: Int, startDate: Long, notes: String, paidMonthsCount: Int) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var provider by remember { mutableStateOf("TABBY") } // "TABBY", "TAMARA", "SNB_FLEX", "OTHER"
  var totalAmountStr by remember { mutableStateOf("") }
  var monthsCount by remember { mutableIntStateOf(4) }
  var notes by remember { mutableStateOf("") }

  // Start Date / Month & Year
  val currentCal = remember { java.util.Calendar.getInstance() }
  var startMonth by remember { mutableIntStateOf(currentCal.get(java.util.Calendar.MONTH) + 1) }
  var startYear by remember { mutableIntStateOf(currentCal.get(java.util.Calendar.YEAR)) }

  // Existing/old plan toggle
  var isOldPlan by remember { mutableStateOf(false) }
  var paidMonthsCount by remember { mutableIntStateOf(0) }

  // SNB FLEX is strictly 4 months fixed
  val effectiveMonths = when (provider) {
    "SNB_FLEX" -> 4
    "TABBY", "TAMARA" -> monthsCount.coerceIn(2, 24)
    else -> monthsCount.coerceAtLeast(1)
  }

  val totalAmount = totalAmountStr.toDoubleOrNull() ?: 0.0
  val calculatedMonthly = if (effectiveMonths > 0 && totalAmount > 0) {
    Math.round((totalAmount / effectiveMonths) * 100.0) / 100.0
  } else 0.0

  val effectivePaid = if (isOldPlan) paidMonthsCount.coerceIn(0, effectiveMonths) else 0

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = AppStrings.get("add_installment", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(AppStrings.get("order_title", language)) },
          placeholder = { Text(AppStrings.get("order_title_hint", language)) },
          modifier = Modifier.fillMaxWidth().testTag("installment_title_input"),
          singleLine = true
        )

        Text(
          text = AppStrings.get("provider", language),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = provider == "TABBY",
            onClick = {
              provider = "TABBY"
              if (monthsCount == 0) monthsCount = 4
            },
            label = { Text("Tabby (تابي)", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = TabbyGreen.copy(alpha = 0.3f)
            )
          )
          FilterChip(
            selected = provider == "TAMARA",
            onClick = {
              provider = "TAMARA"
              if (monthsCount == 0) monthsCount = 4
            },
            label = { Text("Tamara (تمارا)", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = TamaraOrange.copy(alpha = 0.3f)
            )
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = provider == "SNB_FLEX",
            onClick = {
              provider = "SNB_FLEX"
              monthsCount = 4 // strictly 4 months fixed
            },
            label = { Text("SNB FLEX (الأهلي فليكس)", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SnbGreen.copy(alpha = 0.3f)
            )
          )
          FilterChip(
            selected = provider == "OTHER",
            onClick = { provider = "OTHER" },
            label = { Text(AppStrings.get("other_provider", language), fontSize = 11.sp) }
          )
        }

        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
          )
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (provider == "SNB_FLEX") {
                AppStrings.get("snb_flex_notice", language)
              } else {
                AppStrings.get("tabby_tamara_notice", language)
              },
              style = MaterialTheme.typography.bodySmall,
              fontSize = 12.sp
            )
          }
        }

        OutlinedTextField(
          value = totalAmountStr,
          onValueChange = { totalAmountStr = it },
          label = { Text(AppStrings.get("total_order_amount", language) + " (${AppStrings.get("currency", language)})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth().testTag("installment_amount_input"),
          singleLine = true
        )

        // Months Count
        if (provider == "SNB_FLEX") {
          Card(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = AppStrings.get("installments_count", language),
                style = MaterialTheme.typography.bodyMedium
              )
              Text(
                text = "4 ${AppStrings.get("months", language)} (ثابتة)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SnbGreen
              )
            }
          }
        } else {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = AppStrings.get("installments_count", language),
                style = MaterialTheme.typography.labelMedium
              )
              Text(
                text = "$effectiveMonths ${AppStrings.get("months", language)}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Slider(
              value = effectiveMonths.toFloat(),
              onValueChange = { monthsCount = it.toInt() },
              valueRange = 2f..24f,
              steps = 21,
              modifier = Modifier.fillMaxWidth().testTag("installment_months_slider")
            )
          }
        }

        // Starting Month & Year Selector
        MonthYearSelector(
          selectedMonth = startMonth,
          selectedYear = startYear,
          language = language,
          label = AppStrings.get("start_date", language),
          onMonthChange = { startMonth = it },
          onYearChange = { startYear = it }
        )

        // Existing / Old Plan Toggle
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (language == AppLanguage.ARABIC) "خطة قائمة / قديمة (سارية مسبقاً)؟" else "Old / Already Ongoing Plan?",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
              )
              Switch(checked = isOldPlan, onCheckedChange = { isOldPlan = it })
            }

            if (isOldPlan) {
              Text(
                text = "${AppStrings.get("paid_installments", language)}: $effectivePaid / $effectiveMonths ${AppStrings.get("months", language)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
              )
              Slider(
                value = effectivePaid.toFloat(),
                onValueChange = { paidMonthsCount = it.toInt() },
                valueRange = 0f..effectiveMonths.toFloat(),
                steps = if (effectiveMonths > 1) effectiveMonths - 1 else 0,
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }

        // Real-time calculation highlight card
        if (calculatedMonthly > 0) {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = AppStrings.get("monthly_installment", language),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "${String.format("%.2f", calculatedMonthly)} ${AppStrings.get("currency", language)} / ${AppStrings.get("month", language)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "${AppStrings.get("plan_timeline", language)}: ${formatMonthYear(startMonth, startYear, language)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
            }
          }
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(AppStrings.get("notes", language)) },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && totalAmount > 0) {
            val timestamp = buildDateTimestamp(startMonth, startYear)
            onConfirm(
              title,
              provider,
              totalAmount,
              effectiveMonths,
              timestamp,
              notes,
              effectivePaid
            )
          }
        },
        modifier = Modifier.testTag("save_installment_button")
      ) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}

@Composable
fun EditInstallmentDialog(
  installment: com.example.data.local.entity.InstallmentPlanEntity,
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (
    id: Long,
    title: String,
    provider: String,
    totalAmount: Double,
    monthsCount: Int,
    startDate: Long,
    paidMonthsCount: Int,
    notes: String
  ) -> Unit
) {
  var title by remember { mutableStateOf(installment.title) }
  var provider by remember { mutableStateOf(installment.provider) }
  var totalAmountStr by remember { mutableStateOf(installment.totalAmount.toString()) }
  var monthsCount by remember { mutableIntStateOf(installment.monthsCount) }
  var notes by remember { mutableStateOf(installment.notes) }

  // Start Date / Month & Year
  var startMonth by remember { mutableIntStateOf(getMonthFromTimestamp(installment.startDate)) }
  var startYear by remember { mutableIntStateOf(getYearFromTimestamp(installment.startDate)) }
  var paidMonthsCount by remember { mutableIntStateOf(installment.paidMonthsCount) }

  val effectiveMonths = when (provider) {
    "SNB_FLEX" -> 4
    "TABBY", "TAMARA" -> monthsCount.coerceIn(2, 24)
    else -> monthsCount.coerceAtLeast(1)
  }

  val totalAmount = totalAmountStr.toDoubleOrNull() ?: 0.0
  val calculatedMonthly = if (effectiveMonths > 0 && totalAmount > 0) {
    Math.round((totalAmount / effectiveMonths) * 100.0) / 100.0
  } else 0.0

  val safePaid = paidMonthsCount.coerceIn(0, effectiveMonths)
  val remainingMonths = (effectiveMonths - safePaid).coerceAtLeast(0)
  val remainingAmount = (totalAmount - (calculatedMonthly * safePaid)).coerceAtLeast(0.0)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = AppStrings.get("edit_installment", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(AppStrings.get("order_title", language)) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Text(
          text = AppStrings.get("provider", language),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = provider == "TABBY",
            onClick = { provider = "TABBY" },
            label = { Text("Tabby (تابي)", fontSize = 11.sp) }
          )
          FilterChip(
            selected = provider == "TAMARA",
            onClick = { provider = "TAMARA" },
            label = { Text("Tamara (تمارا)", fontSize = 11.sp) }
          )
          FilterChip(
            selected = provider == "SNB_FLEX",
            onClick = {
              provider = "SNB_FLEX"
              monthsCount = 4
            },
            label = { Text("SNB FLEX", fontSize = 11.sp) }
          )
        }

        OutlinedTextField(
          value = totalAmountStr,
          onValueChange = { totalAmountStr = it },
          label = { Text(AppStrings.get("total_order_amount", language) + " (${AppStrings.get("currency", language)})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        // Months Count
        if (provider != "SNB_FLEX") {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = AppStrings.get("installments_count", language),
                style = MaterialTheme.typography.labelMedium
              )
              Text(
                text = "$effectiveMonths ${AppStrings.get("months", language)}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Slider(
              value = effectiveMonths.toFloat(),
              onValueChange = { monthsCount = it.toInt() },
              valueRange = 2f..24f,
              steps = 21,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        // Starting Month & Year Selector
        MonthYearSelector(
          selectedMonth = startMonth,
          selectedYear = startYear,
          language = language,
          label = AppStrings.get("start_date", language),
          onMonthChange = { startMonth = it },
          onYearChange = { startYear = it }
        )

        // Paid months count slider
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = AppStrings.get("paid_installments", language),
              style = MaterialTheme.typography.labelMedium
            )
            Text(
              text = "$safePaid / $effectiveMonths ${AppStrings.get("months", language)}",
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
          Slider(
            value = safePaid.toFloat(),
            onValueChange = { paidMonthsCount = it.toInt() },
            valueRange = 0f..effectiveMonths.toFloat(),
            steps = if (effectiveMonths > 1) effectiveMonths - 1 else 0,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Real-time timeline & remaining details card
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "${AppStrings.get("monthly_installment", language)}: ${String.format("%.2f", calculatedMonthly)} ${AppStrings.get("currency", language)}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "${AppStrings.get("start_date", language)}: ${formatMonthYear(startMonth, startYear, language)}",
              style = MaterialTheme.typography.bodySmall
            )
            Text(
              text = "${AppStrings.get("remaining_loan", language)}: ${String.format("%.2f", remainingAmount)} ${AppStrings.get("currency", language)} ($remainingMonths ${AppStrings.get("months", language)})",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(AppStrings.get("notes", language)) },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && totalAmount > 0) {
            val timestamp = buildDateTimestamp(startMonth, startYear)
            onConfirm(
              installment.id,
              title,
              provider,
              totalAmount,
              effectiveMonths,
              timestamp,
              safePaid,
              notes
            )
          }
        }
      ) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}

@Composable
fun EditCarLoanDialog(
  currentCar: CarLoanEntity?,
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (
    carName: String,
    totalPrice: Double,
    downPayment: Double,
    termMonths: Int,
    annualProfitRate: Double,
    balloonPayment: Double,
    startDate: Long,
    paidMonths: Int,
    balloonRefinanced: Boolean,
    balloonBankName: String,
    balloonTermMonths: Int
  ) -> Unit
) {
  var carName by remember { mutableStateOf(currentCar?.carName ?: "") }
  var totalPriceStr by remember { mutableStateOf(currentCar?.totalCarPrice?.let { if (it > 0) it.toString() else "" } ?: "") }
  var downPaymentStr by remember { mutableStateOf(currentCar?.downPayment?.let { if (it > 0) it.toString() else "" } ?: "") }
  var termMonths by remember { mutableIntStateOf(currentCar?.termMonths ?: 60) }
  var profitRateStr by remember { mutableStateOf(currentCar?.annualProfitRate?.let { if (it > 0) it.toString() else "" } ?: "") }
  var balloonPaymentStr by remember { mutableStateOf(currentCar?.balloonPayment?.let { if (it > 0) it.toString() else "" } ?: "") }
  var paidMonths by remember { mutableIntStateOf(currentCar?.paidMonths ?: 0) }

  // Start Date / Month & Year
  var startMonth by remember { mutableIntStateOf(getMonthFromTimestamp(currentCar?.startDate ?: System.currentTimeMillis())) }
  var startYear by remember { mutableIntStateOf(getYearFromTimestamp(currentCar?.startDate ?: System.currentTimeMillis())) }

  // Balloon refinancing options per user request
  var balloonRefinanced by remember { mutableStateOf(currentCar?.balloonRefinanced ?: false) }
  var balloonBankName by remember { mutableStateOf(currentCar?.balloonBankName ?: "Al Rajhi Bank") }
  var balloonTermMonths by remember { mutableIntStateOf(currentCar?.balloonTermMonths ?: 24) }

  val banks = listOf(
    "bank_alrajhi",
    "bank_snb",
    "bank_riyad",
    "bank_alinma",
    "bank_albilad",
    "bank_other"
  )

  // Maturity calculation
  val maturityCal = remember(startMonth, startYear, termMonths) {
    val c = java.util.Calendar.getInstance()
    c.set(java.util.Calendar.YEAR, startYear)
    c.set(java.util.Calendar.MONTH, startMonth - 1)
    c.add(java.util.Calendar.MONTH, termMonths)
    c
  }
  val maturityMonth = maturityCal.get(java.util.Calendar.MONTH) + 1
  val maturityYear = maturityCal.get(java.util.Calendar.YEAR)

  val safePaid = paidMonths.coerceIn(0, termMonths)
  val remainingMonths = (termMonths - safePaid).coerceAtLeast(0)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (currentCar == null) AppStrings.get("add_car_plan", language) else AppStrings.get("edit_car_loan", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = carName,
          onValueChange = { carName = it },
          label = { Text(AppStrings.get("car_name", language)) },
          modifier = Modifier.fillMaxWidth().testTag("car_name_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = totalPriceStr,
          onValueChange = { totalPriceStr = it },
          label = { Text(AppStrings.get("car_total_price", language)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth().testTag("car_price_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = downPaymentStr,
          onValueChange = { downPaymentStr = it },
          label = { Text(AppStrings.get("down_payment", language)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = balloonPaymentStr,
          onValueChange = { balloonPaymentStr = it },
          label = { Text(AppStrings.get("balloon_payment", language)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "${AppStrings.get("financing_term", language)}: $termMonths ${AppStrings.get("months", language)}")
          Row {
            listOf(36, 48, 60).forEach { t ->
              FilterChip(
                selected = termMonths == t,
                onClick = { termMonths = t },
                label = { Text("$t") },
                modifier = Modifier.padding(horizontal = 2.dp)
              )
            }
          }
        }

        OutlinedTextField(
          value = profitRateStr,
          onValueChange = { profitRateStr = it },
          label = { Text(AppStrings.get("profit_rate", language) + " %") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        // Starting Month & Year Selector for Car Loan
        MonthYearSelector(
          selectedMonth = startMonth,
          selectedYear = startYear,
          language = language,
          label = "${AppStrings.get("start_date", language)} (${AppStrings.get("car_section", language)})",
          onMonthChange = { startMonth = it },
          onYearChange = { startYear = it }
        )

        // Paid months count slider
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = AppStrings.get("paid_installments", language),
              style = MaterialTheme.typography.labelMedium
            )
            Text(
              text = "$safePaid / $termMonths ${AppStrings.get("months", language)}",
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
          Slider(
            value = safePaid.toFloat(),
            onValueChange = { paidMonths = it.toInt() },
            valueRange = 0f..termMonths.toFloat(),
            steps = if (termMonths > 1) termMonths - 1 else 0,
            modifier = Modifier.fillMaxWidth().testTag("car_paid_months_slider")
          )
        }

        // Loan Timeline info card
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "${AppStrings.get("plan_timeline", language)}: ${formatMonthYear(startMonth, startYear, language)} ➔ ${formatMonthYear(maturityMonth, maturityYear, language)}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "${AppStrings.get("months_elapsed", language)}: $safePaid ${AppStrings.get("months", language)} • ${AppStrings.get("remaining_loan", language)}: $remainingMonths ${AppStrings.get("months", language)}",
              style = MaterialTheme.typography.labelSmall
            )
          }
        }

        // Reschedule Balloon payment option
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = AppStrings.get("cannot_pay_balloon", language),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
              )
              Switch(
                checked = balloonRefinanced,
                onCheckedChange = { balloonRefinanced = it }
              )
            }

            if (balloonRefinanced) {
              Text(
                text = AppStrings.get("reschedule_bank", language),
                style = MaterialTheme.typography.labelSmall
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                banks.take(3).forEach { bKey ->
                  val bName = AppStrings.get(bKey, language)
                  FilterChip(
                    selected = balloonBankName == bName,
                    onClick = { balloonBankName = bName },
                    label = { Text(bName.take(10), fontSize = 10.sp) }
                  )
                }
              }

              Text(
                text = "${AppStrings.get("reschedule_term", language)}: $balloonTermMonths ${AppStrings.get("months", language)}",
                style = MaterialTheme.typography.labelSmall
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                listOf(12, 24, 36).forEach { t ->
                  FilterChip(
                    selected = balloonTermMonths == t,
                    onClick = { balloonTermMonths = t },
                    label = { Text("$t ${AppStrings.get("months", language)}", fontSize = 11.sp) }
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val tot = totalPriceStr.toDoubleOrNull() ?: 0.0
          val dp = downPaymentStr.toDoubleOrNull() ?: 0.0
          val rate = profitRateStr.toDoubleOrNull() ?: 3.5
          val bl = balloonPaymentStr.toDoubleOrNull() ?: 0.0
          val timestamp = buildDateTimestamp(startMonth, startYear)
          if (carName.isNotBlank() && tot > 0) {
            onConfirm(
              carName, tot, dp, termMonths, rate, bl,
              timestamp,
              safePaid, balloonRefinanced, balloonBankName, balloonTermMonths
            )
          }
        },
        modifier = Modifier.testTag("save_car_button")
      ) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}

@Composable
fun AddCarServiceDialog(
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (serviceType: String, cost: Double, currentOdo: Int, nextDueOdo: Int, nextDueDate: Long, notes: String) -> Unit
) {
  var serviceType by remember { mutableStateOf("OIL_FILTER") }
  var costStr by remember { mutableStateOf("") }
  var currentOdoStr by remember { mutableStateOf("25000") }
  var nextDueOdoStr by remember { mutableStateOf("35000") }
  var notes by remember { mutableStateOf("") }

  val serviceTypes = listOf(
    "OIL_FILTER" to "oil_filter_change",
    "INSPECTION" to "periodic_inspection",
    "INSURANCE" to "insurance_renewal",
    "TIRES" to "tires_change",
    "BRAKES" to "brake_pads",
    "REGISTRATION" to "registration_renewal",
    "BATTERY" to "car_battery",
    "GENERAL" to "general_maintenance"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = AppStrings.get("add_car_service", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = AppStrings.get("service_type", language),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          serviceTypes.chunked(2).forEach { row ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              row.forEach { (typeKey, strKey) ->
                FilterChip(
                  selected = serviceType == typeKey,
                  onClick = {
                    serviceType = typeKey
                    if (typeKey == "OIL_FILTER") {
                      val curr = currentOdoStr.toIntOrNull() ?: 25000
                      nextDueOdoStr = (curr + 10000).toString()
                    }
                  },
                  label = { Text(AppStrings.get(strKey, language), fontSize = 11.sp) },
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = costStr,
          onValueChange = { costStr = it },
          label = { Text(AppStrings.get("service_cost", language) + " (${AppStrings.get("currency", language)})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = currentOdoStr,
          onValueChange = {
            currentOdoStr = it
            val curr = it.toIntOrNull() ?: 0
            if (serviceType == "OIL_FILTER") {
              nextDueOdoStr = (curr + 10000).toString()
            }
          },
          label = { Text(AppStrings.get("current_odometer", language)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = nextDueOdoStr,
          onValueChange = { nextDueOdoStr = it },
          label = { Text(AppStrings.get("next_service_odometer", language)) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(AppStrings.get("notes", language)) },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val cost = costStr.toDoubleOrNull() ?: 0.0
          val currOdo = currentOdoStr.toIntOrNull() ?: 0
          val nextOdo = nextDueOdoStr.toIntOrNull() ?: (currOdo + 10000)
          val dueDate = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000)
          if (cost > 0) {
            onConfirm(serviceType, cost, currOdo, nextOdo, dueDate, notes)
          }
        }
      ) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}

@Composable
fun AddGoldHoldingDialog(
  language: AppLanguage,
  defaultPrice24k: Double,
  onDismiss: () -> Unit,
  onConfirm: (itemType: String, karat: Int, weightGrams: Double, purchasePricePerGram: Double, purchaseDate: Long, notes: String) -> Unit
) {
  var itemType by remember { mutableStateOf("BULLION_24K") }
  var karat by remember { mutableIntStateOf(24) }
  var weightStr by remember { mutableStateOf("") }
  var pricePerGramStr by remember { mutableStateOf(defaultPrice24k.toString()) }
  var notes by remember { mutableStateOf("") }

  val weight = weightStr.toDoubleOrNull() ?: 0.0
  val pricePerGram = pricePerGramStr.toDoubleOrNull() ?: 0.0
  val totalCost = weight * pricePerGram

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = AppStrings.get("add_gold_item", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = AppStrings.get("gold_item_name", language),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilterChip(
            selected = itemType == "BULLION_24K",
            onClick = {
              itemType = "BULLION_24K"
              karat = 24
              pricePerGramStr = defaultPrice24k.toString()
            },
            label = { Text("سبيكة عيار 24", fontSize = 11.sp) }
          )
          FilterChip(
            selected = itemType == "COIN_21K",
            onClick = {
              itemType = "COIN_21K"
              karat = 21
              pricePerGramStr = String.format("%.2f", defaultPrice24k * (21.0 / 24.0))
            },
            label = { Text("ليرة / عملة", fontSize = 11.sp) }
          )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilterChip(
            selected = itemType == "JEWELRY_21K",
            onClick = {
              itemType = "JEWELRY_21K"
              karat = 21
              pricePerGramStr = String.format("%.2f", defaultPrice24k * (21.0 / 24.0))
            },
            label = { Text("مجوهرات عيار 21", fontSize = 11.sp) }
          )
          FilterChip(
            selected = itemType == "JEWELRY_18K",
            onClick = {
              itemType = "JEWELRY_18K"
              karat = 18
              pricePerGramStr = String.format("%.2f", defaultPrice24k * (18.0 / 24.0))
            },
            label = { Text("ذهب عيار 18", fontSize = 11.sp) }
          )
        }

        OutlinedTextField(
          value = weightStr,
          onValueChange = { weightStr = it },
          label = { Text(AppStrings.get("weight_in_grams", language) + " (${AppStrings.get("grams", language)})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = pricePerGramStr,
          onValueChange = { pricePerGramStr = it },
          label = { Text(AppStrings.get("purchase_price_per_gram", language) + " (${AppStrings.get("currency", language)})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        if (totalCost > 0) {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = AppStrings.get("total_purchase_cost", language),
                style = MaterialTheme.typography.labelSmall
              )
              Text(
                text = "${String.format("%.2f", totalCost)} ${AppStrings.get("currency", language)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
              )
            }
          }
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(AppStrings.get("notes", language)) },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (weight > 0 && pricePerGram > 0) {
            onConfirm(itemType, karat, weight, pricePerGram, System.currentTimeMillis(), notes)
          }
        }
      ) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}

@Composable
fun AddTelecomDialog(
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (
    provider: String,
    planFee: Double,
    hasDevice: Boolean,
    deviceModel: String,
    deviceCost: Double,
    commitmentMonths: Int,
    contractCount: Int,
    notes: String
  ) -> Unit
) {
  var provider by remember { mutableStateOf("STC") }
  var planFeeStr by remember { mutableStateOf("230") }
  var contractCount by remember { mutableIntStateOf(1) }
  var hasDevice by remember { mutableStateOf(true) }
  var deviceModel by remember { mutableStateOf("iPhone 16 Pro Max") }
  var deviceCostStr by remember { mutableStateOf("5400") }
  var commitmentMonths by remember { mutableIntStateOf(24) }
  var notes by remember { mutableStateOf("") }

  val deviceCost = deviceCostStr.toDoubleOrNull() ?: 0.0
  val calculatedDeviceMonthly = if (hasDevice && commitmentMonths > 0) {
    Math.round((deviceCost / commitmentMonths) * 100.0) / 100.0
  } else 0.0

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = AppStrings.get("add_telecom_contract", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = AppStrings.get("provider_name", language),
          style = MaterialTheme.typography.labelMedium
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("STC", "Mobily", "Zain", "Salam").forEach { p ->
            FilterChip(
              selected = provider == p,
              onClick = { provider = p },
              label = { Text(p, fontSize = 11.sp) }
            )
          }
        }

        OutlinedTextField(
          value = planFeeStr,
          onValueChange = { planFeeStr = it },
          label = { Text(AppStrings.get("monthly_plan_cost", language) + " (${AppStrings.get("currency", language)})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "${AppStrings.get("total_telecom_contracts", language)}: $contractCount")
          Row {
            listOf(1, 2, 3, 4).forEach { count ->
              FilterChip(
                selected = contractCount == count,
                onClick = { contractCount = count },
                label = { Text("$count") },
                modifier = Modifier.padding(horizontal = 2.dp)
              )
            }
          }
        }

        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = AppStrings.get("has_device_commitment", language),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
              )
              Switch(checked = hasDevice, onCheckedChange = { hasDevice = it })
            }

            if (hasDevice) {
              OutlinedTextField(
                value = deviceModel,
                onValueChange = { deviceModel = it },
                label = { Text(AppStrings.get("device_model", language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
              )

              OutlinedTextField(
                value = deviceCostStr,
                onValueChange = { deviceCostStr = it },
                label = { Text(AppStrings.get("device_total_price", language)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = "${AppStrings.get("commitment_period", language)}: $commitmentMonths ${AppStrings.get("months", language)}")
                Row {
                  listOf(12, 18, 24).forEach { m ->
                    FilterChip(
                      selected = commitmentMonths == m,
                      onClick = { commitmentMonths = m },
                      label = { Text("$m") }
                    )
                  }
                }
              }

              if (calculatedDeviceMonthly > 0) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = AppStrings.get("device_monthly_installment", language),
                      style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                      text = "${String.format("%.2f", calculatedDeviceMonthly)} ${AppStrings.get("currency", language)}",
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val fee = planFeeStr.toDoubleOrNull() ?: 0.0
          onConfirm(provider, fee, hasDevice, deviceModel, deviceCost, commitmentMonths, contractCount, notes)
        }
      ) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}

@Composable
fun EditHousingDialog(
  currentHousing: HousingEntity?,
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (
    name: String,
    rent: Double,
    frequency: String,
    water: Double,
    waterIncluded: Boolean,
    electricity: Double,
    elecIncluded: Boolean,
    dueDate: Long,
    notes: String
  ) -> Unit
) {
  var name by remember { mutableStateOf(currentHousing?.propertyName ?: "شقة سكنية") }
  var rentStr by remember { mutableStateOf(currentHousing?.rentAmount?.toString() ?: "3000") }
  var frequency by remember { mutableStateOf(currentHousing?.paymentFrequency ?: "MONTHLY") }
  var waterStr by remember { mutableStateOf(currentHousing?.waterAmount?.toString() ?: "150") }
  var isWaterIncluded by remember { mutableStateOf(currentHousing?.isWaterIncluded ?: true) }
  var elecStr by remember { mutableStateOf(currentHousing?.electricityAmount?.toString() ?: "400") }
  var isElecIncluded by remember { mutableStateOf(currentHousing?.isElectricityIncluded ?: true) }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = AppStrings.get("housing_rent_title", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(if (language == AppLanguage.ARABIC) "اسم العقار / المسكن" else "Property Name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = rentStr,
          onValueChange = { rentStr = it },
          label = { Text(AppStrings.get("housing_rent_amount", language) + " (${AppStrings.get("currency", language)})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Text(text = AppStrings.get("payment_frequency", language), style = MaterialTheme.typography.labelSmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          listOf(
            "MONTHLY" to "monthly",
            "QUARTERLY" to "quarterly",
            "SEMI_ANNUAL" to "semi_annual",
            "ANNUAL" to "annual"
          ).forEach { (fCode, sKey) ->
            FilterChip(
              selected = frequency == fCode,
              onClick = { frequency = fCode },
              label = { Text(AppStrings.get(sKey, language), fontSize = 10.sp) }
            )
          }
        }

        // Water
        Card(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = AppStrings.get("water_included", language),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
              )
              Switch(checked = isWaterIncluded, onCheckedChange = { isWaterIncluded = it })
            }
            if (!isWaterIncluded) {
              OutlinedTextField(
                value = waterStr,
                onValueChange = { waterStr = it },
                label = { Text(AppStrings.get("water_bill", language)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
              )
            }
          }
        }

        // Electricity
        Card(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = AppStrings.get("electricity_included", language),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
              )
              Switch(checked = isElecIncluded, onCheckedChange = { isElecIncluded = it })
            }
            if (!isElecIncluded) {
              OutlinedTextField(
                value = elecStr,
                onValueChange = { elecStr = it },
                label = { Text(AppStrings.get("electricity_bill", language)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val rent = rentStr.toDoubleOrNull() ?: 0.0
          val water = waterStr.toDoubleOrNull() ?: 0.0
          val elec = elecStr.toDoubleOrNull() ?: 0.0
          val dueDate = currentHousing?.nextDueDate ?: (System.currentTimeMillis() + (20L * 24 * 60 * 60 * 1000))
          onConfirm(name, rent, frequency, water, isWaterIncluded, elec, isElecIncluded, dueDate, notes)
        }
      ) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}

@Composable
fun AddIncomeDialog(
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (title: String, amount: Double, category: String, receiptDate: Long, isRecurring: Boolean, notes: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var amountStr by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("SALARY") }
  var isRecurring by remember { mutableStateOf(true) }
  var notes by remember { mutableStateOf("") }

  val categories = listOf(
    "SALARY" to "category_salary",
    "FREELANCE" to "category_freelance",
    "INVESTMENTS" to "category_investments",
    "RENTAL" to "category_rental",
    "BONUS" to "category_bonus",
    "BUSINESS" to "category_business",
    "OTHER" to "category_other_income"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = AppStrings.get("add_income", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(AppStrings.get("income_name", language)) },
          modifier = Modifier.fillMaxWidth().testTag("income_title_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = it },
          label = { Text(AppStrings.get("income_amount", language) + " (${AppStrings.get("currency", language)})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth().testTag("income_amount_input"),
          singleLine = true
        )

        Text(
          text = AppStrings.get("category", language),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          categories.chunked(2).forEach { row ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              row.forEach { (catKey, strKey) ->
                FilterChip(
                  selected = selectedCategory == catKey,
                  onClick = { selectedCategory = catKey },
                  label = { Text(AppStrings.get(strKey, language), fontSize = 11.sp) },
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = AppStrings.get("recurring_monthly_income", language),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
          )
          Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(AppStrings.get("notes", language)) },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountStr.toDoubleOrNull() ?: 0.0
          if (title.isNotBlank() && amt > 0) {
            onConfirm(title, amt, selectedCategory, System.currentTimeMillis(), isRecurring, notes)
          }
        },
        modifier = Modifier.testTag("save_income_button")
      ) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}

@Composable
fun AddCustomAlertDialog(
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (title: String, alertType: String, targetAmount: Double, dueDate: Long, thresholdPercentage: Int, reason: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var alertType by remember { mutableStateOf("PAYMENT_REMINDER") }
  var targetAmountStr by remember { mutableStateOf("1500") }
  var thresholdPercentage by remember { mutableIntStateOf(80) }
  var reason by remember { mutableStateOf("") }
  var daysUntilDue by remember { mutableIntStateOf(5) }

  val alertTypes = listOf(
    "PAYMENT_REMINDER" to "alert_type_payment",
    "BUDGET_THRESHOLD" to "alert_type_budget",
    "SERVICE_REMINDER" to "alert_type_service",
    "GENERAL" to "alert_type_general"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = AppStrings.get("add_custom_alert", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text(AppStrings.get("alert_title", language)) },
          modifier = Modifier.fillMaxWidth().testTag("alert_title_input"),
          singleLine = true
        )

        Text(
          text = AppStrings.get("alert_type", language),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          alertTypes.chunked(2).forEach { row ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              row.forEach { (typeKey, strKey) ->
                FilterChip(
                  selected = alertType == typeKey,
                  onClick = { alertType = typeKey },
                  label = { Text(AppStrings.get(strKey, language), fontSize = 11.sp) },
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = targetAmountStr,
          onValueChange = { targetAmountStr = it },
          label = { Text(AppStrings.get("alert_target_amount", language) + " (${AppStrings.get("currency", language)})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        if (alertType == "BUDGET_THRESHOLD") {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = AppStrings.get("threshold_percentage", language),
                style = MaterialTheme.typography.bodySmall
              )
              Text(
                text = "$thresholdPercentage%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Slider(
              value = thresholdPercentage.toFloat(),
              onValueChange = { thresholdPercentage = it.toInt() },
              valueRange = 50f..100f,
              steps = 4,
              modifier = Modifier.fillMaxWidth()
            )
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (language == AppLanguage.ARABIC) "التذكير قبل (أيام):" else "Remind before (days):",
              style = MaterialTheme.typography.bodySmall
            )
            Row {
              listOf(1, 3, 5, 7, 10).forEach { d ->
                FilterChip(
                  selected = daysUntilDue == d,
                  onClick = { daysUntilDue = d },
                  label = { Text("$d") },
                  modifier = Modifier.padding(horizontal = 2.dp)
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = reason,
          onValueChange = { reason = it },
          label = { Text(AppStrings.get("alert_reason", language)) },
          placeholder = { Text(AppStrings.get("alert_reason_hint", language)) },
          modifier = Modifier.fillMaxWidth().testTag("alert_reason_input"),
          maxLines = 3
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = targetAmountStr.toDoubleOrNull() ?: 0.0
          val due = System.currentTimeMillis() + (daysUntilDue.toLong() * 24 * 60 * 60 * 1000)
          if (title.isNotBlank()) {
            onConfirm(title, alertType, amt, due, thresholdPercentage, reason)
          }
        },
        modifier = Modifier.testTag("save_alert_button")
      ) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}

@Composable
fun SetBudgetGoalDialog(
  currentGoal: Double,
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (Double) -> Unit
) {
  var goalText by remember { mutableStateOf(if (currentGoal > 0) currentGoal.toString() else "") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = AppStrings.get("monthly_budget_goal", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = AppStrings.get("budget_goal_desc", language),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
          value = goalText,
          onValueChange = { goalText = it },
          label = { Text(AppStrings.get("enter_budget_amount", language)) },
          modifier = Modifier.fillMaxWidth().testTag("budget_goal_input"),
          keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
          ),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = goalText.toDoubleOrNull() ?: 0.0
          onConfirm(amt)
        },
        modifier = Modifier.testTag("save_budget_button")
      ) {
        Text(AppStrings.get("save_budget", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}

@Composable
fun AddRentalDialog(
  language: AppLanguage,
  onDismiss: () -> Unit,
  onConfirm: (
    title: String,
    type: String,
    amount: Double,
    freq: String,
    waterIncluded: Boolean,
    elecIncluded: Boolean,
    durationMonths: Int,
    locationOrPlate: String,
    notes: String
  ) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var rentalType by remember { mutableStateOf("HOUSE") } // HOUSE, STORE, CAR, PLACE
  var amountStr by remember { mutableStateOf("") }
  var paymentFreq by remember { mutableStateOf("MONTHLY") } // MONTHLY, QUARTERLY, ANNUAL
  var waterIncluded by remember { mutableStateOf(false) }
  var elecIncluded by remember { mutableStateOf(false) }
  var durationMonthsStr by remember { mutableStateOf("12") }
  var locationOrPlate by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = AppStrings.get("add_rental", language),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Rental Type Chips
        Text(text = AppStrings.get("category", language), style = MaterialTheme.typography.labelMedium)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = rentalType == "HOUSE",
            onClick = { rentalType = "HOUSE" },
            label = { Text(AppStrings.get("rental_type_house", language), fontSize = 11.sp) }
          )
          FilterChip(
            selected = rentalType == "STORE",
            onClick = { rentalType = "STORE" },
            label = { Text(AppStrings.get("rental_type_store", language), fontSize = 11.sp) }
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = rentalType == "CAR",
            onClick = { rentalType = "CAR" },
            label = { Text(AppStrings.get("rental_type_car", language), fontSize = 11.sp) }
          )
          FilterChip(
            selected = rentalType == "PLACE",
            onClick = { rentalType = "PLACE" },
            label = { Text(AppStrings.get("rental_type_place", language), fontSize = 11.sp) }
          )
        }

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = {
            Text(
              when (rentalType) {
                "HOUSE" -> if (language == AppLanguage.ARABIC) "اسم العقار / الشقة" else "House / Apartment Name"
                "STORE" -> if (language == AppLanguage.ARABIC) "اسم المحل / المتجر" else "Store / Shop Name"
                "CAR" -> if (language == AppLanguage.ARABIC) "موديل واسم السيارة" else "Car Model & Make"
                else -> if (language == AppLanguage.ARABIC) "اسم المكان / المستودع" else "Place / Storage Name"
              }
            )
          },
          modifier = Modifier.fillMaxWidth().testTag("rental_title_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = it },
          label = { Text("${AppStrings.get("amount", language)} (${AppStrings.get("currency", language)})") },
          modifier = Modifier.fillMaxWidth().testTag("rental_amount_input"),
          keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
          ),
          singleLine = true
        )

        // Payment Cycle
        Text(text = if (language == AppLanguage.ARABIC) "دورة السداد" else "Payment Cycle", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilterChip(
            selected = paymentFreq == "MONTHLY",
            onClick = { paymentFreq = "MONTHLY" },
            label = { Text(if (language == AppLanguage.ARABIC) "شهري" else "Monthly", fontSize = 11.sp) }
          )
          FilterChip(
            selected = paymentFreq == "QUARTERLY",
            onClick = { paymentFreq = "QUARTERLY" },
            label = { Text(if (language == AppLanguage.ARABIC) "ربع سنوي (3 أشهر)" else "Quarterly", fontSize = 11.sp) }
          )
          FilterChip(
            selected = paymentFreq == "ANNUAL",
            onClick = { paymentFreq = "ANNUAL" },
            label = { Text(if (language == AppLanguage.ARABIC) "سنوي" else "Annual", fontSize = 11.sp) }
          )
        }

        if (rentalType == "HOUSE" || rentalType == "STORE" || rentalType == "PLACE") {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { waterIncluded = !waterIncluded }
          ) {
            Checkbox(checked = waterIncluded, onCheckedChange = { waterIncluded = it })
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = AppStrings.get("water_included", language), style = MaterialTheme.typography.bodySmall)
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { elecIncluded = !elecIncluded }
          ) {
            Checkbox(checked = elecIncluded, onCheckedChange = { elecIncluded = it })
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = AppStrings.get("electricity_included", language), style = MaterialTheme.typography.bodySmall)
          }
        }

        OutlinedTextField(
          value = locationOrPlate,
          onValueChange = { locationOrPlate = it },
          label = { Text(AppStrings.get("location_or_plate", language)) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = durationMonthsStr,
          onValueChange = { durationMonthsStr = it },
          label = { Text(if (language == AppLanguage.ARABIC) "مدة العقد (بالأشهر)" else "Contract Duration (Months)") },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
          ),
          singleLine = true
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(AppStrings.get("notes", language)) },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountStr.toDoubleOrNull() ?: 0.0
          val dur = durationMonthsStr.toIntOrNull() ?: 12
          if (title.isNotBlank() && amt > 0) {
            onConfirm(title, rentalType, amt, paymentFreq, waterIncluded, elecIncluded, dur, locationOrPlate, notes)
          }
        },
        modifier = Modifier.testTag("save_rental_button")
      ) {
        Text(AppStrings.get("save", language))
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}

