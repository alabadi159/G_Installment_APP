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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.HousingEntity
import com.example.data.local.entity.TelecomContractEntity
import com.example.ui.dialogs.AddExpenseDialog
import com.example.ui.dialogs.AddTelecomDialog
import com.example.ui.dialogs.EditHousingDialog
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommitmentsScreen(
  viewModel: FinanceViewModel,
  modifier: Modifier = Modifier
) {
  val language by viewModel.currentLanguage.collectAsState()
  val overview by viewModel.overview.collectAsState()
  val telecomList by viewModel.telecomContracts.collectAsState()
  val housing by viewModel.housing.collectAsState()
  val expenses by viewModel.expenses.collectAsState()

  var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Telecom, 1: Housing, 2: Daily Expenses
  var showAddTelecomDialog by remember { mutableStateOf(false) }
  var showEditHousingDialog by remember { mutableStateOf(false) }
  var showAddExpenseDialog by remember { mutableStateOf(false) }

  val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale(language.code))

  val subTabs = listOf(
    AppStrings.get("telecom_section", language),
    AppStrings.get("housing_section", language),
    AppStrings.get("daily_expenses_section", language)
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

      when (selectedSubTab) {
        0 -> TelecomTab(
          contracts = telecomList,
          monthlyTotal = overview.telecomMonthlyTotal,
          language = language,
          onDelete = { viewModel.deleteTelecomContract(it) }
        )
        1 -> HousingTab(
          housing = housing,
          monthlyBurden = overview.housingMonthlyTotal,
          language = language,
          dateFormat = dateFormat,
          onEditClick = { showEditHousingDialog = true }
        )
        2 -> DailyExpensesTab(
          expenses = expenses,
          monthlyTotal = overview.currentMonthExpensesTotal,
          language = language,
          dateFormat = dateFormat,
          onDelete = { viewModel.deleteExpense(it) }
        )
      }
    }

    if (selectedSubTab == 0 || selectedSubTab == 2) {
      FloatingActionButton(
        onClick = {
          if (selectedSubTab == 0) showAddTelecomDialog = true
          else showAddExpenseDialog = true
        },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(20.dp)
          .testTag("commitments_fab"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      ) {
        Icon(Icons.Default.Add, contentDescription = null)
      }
    }
  }

  if (showAddTelecomDialog) {
    AddTelecomDialog(
      language = language,
      onDismiss = { showAddTelecomDialog = false },
      onConfirm = { p, fee, hasDev, model, cost, m, count, notes ->
        viewModel.addTelecomContract(p, fee, hasDev, model, cost, m, count, System.currentTimeMillis(), notes)
        showAddTelecomDialog = false
      }
    )
  }

  if (showEditHousingDialog) {
    EditHousingDialog(
      currentHousing = housing,
      language = language,
      onDismiss = { showEditHousingDialog = false },
      onConfirm = { name, rent, freq, water, wInc, elec, eInc, due, notes ->
        viewModel.saveHousing(name, rent, freq, water, wInc, elec, eInc, due, notes)
        showEditHousingDialog = false
      }
    )
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
}

@Composable
private fun TelecomTab(
  contracts: List<TelecomContractEntity>,
  monthlyTotal: Double,
  language: AppLanguage,
  onDelete: (TelecomContractEntity) -> Unit
) {
  val totalContractsCount = contracts.sumOf { it.contractCount }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item { Spacer(modifier = Modifier.height(10.dp)) }

    // Telecom Hero Card
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
                colors = listOf(Color(0xFF5B21B6), Color(0xFF6D28D9), Color(0xFF1E293B))
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
                  text = AppStrings.get("telecom_monthly", language),
                  style = MaterialTheme.typography.labelMedium,
                  color = Color(0xFFDDD6FE)
                )
                Text(
                  text = "${String.format("%,.0f", monthlyTotal)} ${AppStrings.get("currency", language)} / ${AppStrings.get("month", language)}",
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
                  Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFFA78BFA))
                }
              }
            }

            Text(
              text = "${AppStrings.get("total_telecom_contracts", language)}: $totalContractsCount ${AppStrings.get("contract", language)}",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFFEDE9FE),
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }

    item {
      Text(
        text = AppStrings.get("contracts_list", language),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    items(contracts, key = { it.id }) { contract ->
      TelecomItemCard(contract = contract, language = language, onDelete = { onDelete(contract) })
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}

@Composable
fun TelecomItemCard(
  contract: TelecomContractEntity,
  language: AppLanguage,
  onDelete: () -> Unit
) {
  val totalMonthly = (contract.planMonthlyFee * contract.contractCount) + contract.deviceMonthlyCost

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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF8B5CF6))
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(text = contract.providerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
              text = "${contract.contractCount} ${AppStrings.get("contract", language)} • باقة: ${contract.planMonthlyFee} ${AppStrings.get("currency", language)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        }
      }

      // Device Commitment Box if present
      if (contract.hasDeviceCommitment) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Smartphone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(text = contract.deviceName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(
                  text = "التزام: ${contract.deviceCommitmentMonths} شهراً (سعر الجهاز: ${contract.deviceTotalCost})",
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "${String.format("%,.0f", contract.deviceMonthlyCost)} ${AppStrings.get("currency", language)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Text(text = "قسط شهري", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "إجمالي فاتورة الخط شهرياً", style = MaterialTheme.typography.labelMedium)
        Text(
          text = "${String.format("%,.0f", totalMonthly)} ${AppStrings.get("currency", language)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.ExtraBold,
          color = Color(0xFF8B5CF6)
        )
      }
    }
  }
}

@Composable
private fun HousingTab(
  housing: HousingEntity?,
  monthlyBurden: Double,
  language: AppLanguage,
  dateFormat: SimpleDateFormat,
  onEditClick: () -> Unit
) {
  val house = housing ?: HousingEntity()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item { Spacer(modifier = Modifier.height(10.dp)) }

    // Housing Hero Card
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
                colors = listOf(Color(0xFF047857), Color(0xFF065F46), Color(0xFF1E293B))
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
                  text = AppStrings.get("total_monthly_housing_burden", language),
                  style = MaterialTheme.typography.labelMedium,
                  color = Color(0xFFD1FAE5)
                )
                Text(
                  text = "${String.format("%,.0f", monthlyBurden)} ${AppStrings.get("currency", language)} / ${AppStrings.get("month", language)}",
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
              }

              IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
              }
            }

            Text(
              text = house.propertyName,
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.9f),
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }

    // Rent and Utilities Details Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text(
            text = AppStrings.get("housing_rent_title", language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          // Rent Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(text = AppStrings.get("housing_rent_amount", language), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(text = AppStrings.get(house.paymentFrequency.lowercase(), language), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            Text(
              text = "${String.format("%,.0f", house.rentAmount)} ${AppStrings.get("currency", language)}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          // Water Row (شامل أو منفصل)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Opacity, contentDescription = null, tint = Color(0xFF0284C7))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(text = AppStrings.get("water_bill", language), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                  text = if (house.isWaterIncluded) AppStrings.get("water_included", language) else "غير شامل (فاتورة مستقلة)",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (house.isWaterIncluded) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                )
              }
            }
            Text(
              text = if (house.isWaterIncluded) "مشمول" else "${house.waterAmount} ${AppStrings.get("currency", language)}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = if (house.isWaterIncluded) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
            )
          }

          // Electricity Row (شامل أو منفصل)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFF59E0B))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(text = AppStrings.get("electricity_bill", language), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                  text = if (house.isElectricityIncluded) AppStrings.get("electricity_included", language) else "غير شامل (فاتورة مستقلة)",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (house.isElectricityIncluded) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                )
              }
            }
            Text(
              text = if (house.isElectricityIncluded) "مشمول" else "${house.electricityAmount} ${AppStrings.get("currency", language)}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = if (house.isElectricityIncluded) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
            )
          }

          // Next Due Date
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = AppStrings.get("housing_due_date", language), style = MaterialTheme.typography.bodySmall)
            Text(
              text = dateFormat.format(Date(house.nextDueDate)),
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }

    item {
      OutlinedButton(onClick = onEditClick, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(AppStrings.get("save_housing", language))
      }
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}

@Composable
private fun DailyExpensesTab(
  expenses: List<com.example.data.local.entity.ExpenseEntity>,
  monthlyTotal: Double,
  language: AppLanguage,
  dateFormat: SimpleDateFormat,
  onDelete: (com.example.data.local.entity.ExpenseEntity) -> Unit
) {
  var selectedCategoryFilter by remember { mutableStateOf("ALL") }

  val filteredExpenses = if (selectedCategoryFilter == "ALL") {
    expenses
  } else {
    expenses.filter { it.category == selectedCategoryFilter }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item { Spacer(modifier = Modifier.height(10.dp)) }

    // Spending Total Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = AppStrings.get("daily_expenses_this_month", language),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "${String.format("%,.0f", monthlyTotal)} ${AppStrings.get("currency", language)}",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.primary
            )
          }
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(44.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
          }
        }
      }
    }

    // Category Filter Chips
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterChip(
          selected = selectedCategoryFilter == "ALL",
          onClick = { selectedCategoryFilter = "ALL" },
          label = { Text(AppStrings.get("all_tab", language), fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedCategoryFilter == "FOOD",
          onClick = { selectedCategoryFilter = "FOOD" },
          label = { Text(AppStrings.get("category_food", language), fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedCategoryFilter == "SHOPPING",
          onClick = { selectedCategoryFilter = "SHOPPING" },
          label = { Text(AppStrings.get("category_shopping", language), fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedCategoryFilter == "TRANSPORT",
          onClick = { selectedCategoryFilter = "TRANSPORT" },
          label = { Text(AppStrings.get("category_transport", language), fontSize = 11.sp) }
        )
      }
    }

    if (filteredExpenses.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(AppStrings.get("no_expenses_recorded", language), color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    } else {
      items(filteredExpenses, key = { it.id }) { expense ->
        ExpenseItemCard(
          expense = expense,
          language = language,
          dateFormat = dateFormat,
          onDelete = { onDelete(expense) }
        )
      }
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}
