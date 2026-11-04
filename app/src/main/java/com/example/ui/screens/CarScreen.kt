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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CarLoanEntity
import com.example.data.local.entity.CarServiceEntity
import com.example.ui.components.CarLoanBreakdownBar
import com.example.ui.components.HorizontalProgressBar
import com.example.ui.dialogs.AddCarServiceDialog
import com.example.ui.dialogs.EditCarLoanDialog
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CarScreen(
  viewModel: FinanceViewModel,
  modifier: Modifier = Modifier
) {
  val language by viewModel.currentLanguage.collectAsState()
  val carLoan by viewModel.carLoan.collectAsState()
  val carServices by viewModel.carServices.collectAsState()

  var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Financing, 1: After-Sales Services
  var showEditLoanDialog by remember { mutableStateOf(false) }
  var showAddServiceDialog by remember { mutableStateOf(false) }

  val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale(language.code))

  val subTabs = listOf(
    AppStrings.get("car_financing", language),
    AppStrings.get("car_after_sales", language)
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
        // Tab 1: Car Financing & Balloon Payment
        CarFinancingTab(
          carLoan = carLoan,
          language = language,
          onEditClick = { showEditLoanDialog = true }
        )
      } else {
        // Tab 2: After-Sales & Maintenance
        CarServicesTab(
          services = carServices,
          language = language,
          dateFormat = dateFormat,
          onDeleteService = { viewModel.deleteCarService(it) }
        )
      }
    }

    if (selectedSubTab == 1) {
      FloatingActionButton(
        onClick = { showAddServiceDialog = true },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(20.dp)
          .testTag("car_service_fab"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      ) {
        Icon(Icons.Default.Add, contentDescription = AppStrings.get("add_car_service", language))
      }
    }
  }

  if (showEditLoanDialog) {
    EditCarLoanDialog(
      currentCar = carLoan,
      language = language,
      onDismiss = { showEditLoanDialog = false },
      onConfirm = { name, tot, dp, term, rate, bl, start, paid, ref, bName, bTerm ->
        viewModel.updateCarLoan(name, tot, dp, term, rate, bl, start, paid, ref, bName, bTerm)
        showEditLoanDialog = false
      }
    )
  }

  if (showAddServiceDialog) {
    AddCarServiceDialog(
      language = language,
      onDismiss = { showAddServiceDialog = false },
      onConfirm = { type, cost, currOdo, nextOdo, nextDate, notes ->
        viewModel.addCarService(type, cost, currOdo, nextOdo, nextDate, notes)
        showAddServiceDialog = false
      }
    )
  }
}

@Composable
private fun CarFinancingTab(
  carLoan: CarLoanEntity?,
  language: AppLanguage,
  onEditClick: () -> Unit
) {
  val car = carLoan ?: CarLoanEntity()
  val principal = (car.totalCarPrice - car.downPayment - car.balloonPayment).coerceAtLeast(0.0)
  val totalProfit = principal * (car.annualProfitRate / 100.0) * (car.termMonths / 12.0)
  val loanProgress = if (car.termMonths > 0) car.paidMonths.toFloat() / car.termMonths.toFloat() else 0f

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item { Spacer(modifier = Modifier.height(10.dp)) }

    // Car Hero Card
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
                colors = listOf(Color(0xFF1E3A8A), Color(0xFF1E293B), Color(0xFF0F172A))
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
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = car.carName,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
                val startM = com.example.ui.dialogs.getMonthFromTimestamp(car.startDate)
                val startY = com.example.ui.dialogs.getYearFromTimestamp(car.startDate)
                val matCal = java.util.Calendar.getInstance().apply {
                  timeInMillis = car.startDate
                  add(java.util.Calendar.MONTH, car.termMonths)
                }
                val matM = matCal.get(java.util.Calendar.MONTH) + 1
                val matY = matCal.get(java.util.Calendar.YEAR)
                Text(
                  text = "${AppStrings.get("start_date", language)}: ${com.example.ui.dialogs.formatMonthYear(startM, startY, language)} ➔ ${AppStrings.get("maturity_date", language)}: ${com.example.ui.dialogs.formatMonthYear(matM, matY, language)}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF93C5FD)
                )
                Text(
                  text = "${AppStrings.get("financing_term", language)}: ${car.termMonths} ${AppStrings.get("months", language)} (60 شهراً)",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFBFDBFE).copy(alpha = 0.8f)
                )
              }
              IconButton(onClick = onEditClick, modifier = Modifier.testTag("edit_car_loan_button")) {
                Icon(Icons.Default.Edit, contentDescription = AppStrings.get("edit_car_loan", language), tint = Color.White)
              }
            }

            // Monthly Installment Highlight
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Bottom
            ) {
              Column {
                Text(
                  text = AppStrings.get("monthly_installment", language),
                  style = MaterialTheme.typography.labelMedium,
                  color = Color(0xFFBFDBFE)
                )
                Text(
                  text = "${String.format("%,.0f", car.monthlyInstallment)} ${AppStrings.get("currency", language)}",
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF60A5FA)
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = AppStrings.get("car_total_price", language),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFBFDBFE)
                )
                Text(
                  text = "${String.format("%,.0f", car.totalCarPrice)} ${AppStrings.get("currency", language)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }

            // Loan Term Progress
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                val remMonths = (car.termMonths - car.paidMonths).coerceAtLeast(0)
                Text(
                  text = "${AppStrings.get("paid_installments", language)}: ${car.paidMonths} / ${car.termMonths} ${AppStrings.get("months", language)} ($remMonths ${AppStrings.get("months", language)} ${if (language == AppLanguage.ARABIC) "متبقية" else "remaining"})",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                  text = "${(loanProgress * 100).toInt()}%",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF60A5FA)
                )
              }
              HorizontalProgressBar(
                progress = loanProgress,
                color = Color(0xFF60A5FA),
                trackColor = Color.White.copy(alpha = 0.2f),
                height = 8.dp
              )
            }
          }
        }
      }
    }

    // Balloon Payment (الدفعة الأخيرة / دفعة التملك) & Refinancing Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (car.balloonRefinanced) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
          else MaterialTheme.colorScheme.surface
        )
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = AppStrings.get("balloon_payment", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (car.balloonRefinanced) "مُجدولة عبر البنك (${car.balloonBankName})" else "مستحقة دفعة واحدة عند انتهاء الـ 60 شهراً",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Text(
              text = "${String.format("%,.0f", car.balloonPayment)} ${AppStrings.get("currency", language)}",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFFEC4899)
            )
          }

          if (car.balloonRefinanced) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "${AppStrings.get("reschedule_balloon", language)} (${car.balloonTermMonths} ${AppStrings.get("months", language)})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = car.balloonBankName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = AppStrings.get("balloon_monthly", language),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "${String.format("%,.0f", car.balloonMonthlyAmount)} ${AppStrings.get("currency", language)}",
                    style = MaterialTheme.typography.titleMedium,
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

    // Interactive Loan Breakdown Chart
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = AppStrings.get("car_expenses_chart", language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          CarLoanBreakdownBar(
            downPayment = car.downPayment,
            financedPrincipal = principal,
            totalProfit = totalProfit,
            balloonPayment = car.balloonPayment,
            language = language
          )
        }
      }
    }

    item {
      OutlinedButton(
        onClick = onEditClick,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(AppStrings.get("edit_car_loan", language))
      }
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}

@Composable
private fun CarServicesTab(
  services: List<CarServiceEntity>,
  language: AppLanguage,
  dateFormat: SimpleDateFormat,
  onDeleteService: (CarServiceEntity) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item { Spacer(modifier = Modifier.height(10.dp)) }

    // Service Status Highlights
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        ServiceSummaryCard(
          title = AppStrings.get("oil_filter_change", language),
          status = "باقي 5,000 كم",
          icon = Icons.Default.Speed,
          color = Color(0xFF10B981),
          modifier = Modifier.weight(1f)
        )
        ServiceSummaryCard(
          title = AppStrings.get("periodic_inspection", language),
          status = "ساري 45 يوماً",
          icon = Icons.Default.VerifiedUser,
          color = Color(0xFF3B82F6),
          modifier = Modifier.weight(1f)
        )
        ServiceSummaryCard(
          title = AppStrings.get("insurance_renewal", language),
          status = "ساري 85 يوماً",
          icon = Icons.Default.HealthAndSafety,
          color = Color(0xFFF59E0B),
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Services History List
    item {
      Text(
        text = AppStrings.get("service_history", language),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    if (services.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
              text = AppStrings.get("no_service_history", language),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(services, key = { it.id }) { service ->
        CarServiceItemCard(
          service = service,
          language = language,
          dateFormat = dateFormat,
          onDelete = { onDeleteService(service) }
        )
      }
    }

    item { Spacer(modifier = Modifier.height(80.dp)) }
  }
}

@Composable
private fun ServiceSummaryCard(
  title: String,
  status: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1
      )
      Text(
        text = status,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun CarServiceItemCard(
  service: CarServiceEntity,
  language: AppLanguage,
  dateFormat: SimpleDateFormat,
  onDelete: () -> Unit
) {
  val typeLabel = when (service.serviceType) {
    "OIL_FILTER" -> AppStrings.get("oil_filter_change", language)
    "INSPECTION" -> AppStrings.get("periodic_inspection", language)
    "INSURANCE" -> AppStrings.get("insurance_renewal", language)
    "TIRES" -> AppStrings.get("tires_change", language)
    "BRAKES" -> AppStrings.get("brake_pads", language)
    "REGISTRATION" -> AppStrings.get("registration_renewal", language)
    "BATTERY" -> AppStrings.get("car_battery", language)
    else -> AppStrings.get("general_maintenance", language)
  }

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
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
          modifier = Modifier.size(40.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(text = typeLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
          Text(
            text = "${dateFormat.format(Date(service.date))} • عداد: ${service.currentOdometerKm} كم",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "${String.format("%,.0f", service.cost)} ${AppStrings.get("currency", language)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onDelete) {
          Icon(
            Icons.Default.Delete,
            contentDescription = AppStrings.get("delete", language),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}
