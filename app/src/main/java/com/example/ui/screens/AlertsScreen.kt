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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CustomAlertEntity
import com.example.ui.dialogs.AddCustomAlertDialog
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertsScreen(
  viewModel: FinanceViewModel,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val language by viewModel.currentLanguage.collectAsState()
  val alerts by viewModel.alerts.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }

  val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale(language.code))

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item { Spacer(modifier = Modifier.height(10.dp)) }

      // Alerts Header Card
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
                  colors = listOf(Color(0xFF7C2D12), Color(0xFFC2410C), Color(0xFF1E293B))
                )
              )
              .padding(18.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = AppStrings.get("alerts_and_reminders", language),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White
                )
                Text(
                  text = if (language == AppLanguage.ARABIC) "تذكيرات بالمستحقات وتنبيهات بحدود الميزانية" else "Payment reminders & budget threshold warnings",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFFFEDD5)
                )
              }
              Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(44.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFFED7AA))
                }
              }
            }
          }
        }
      }

      if (alerts.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  imageVector = Icons.Default.NotificationsOff,
                  contentDescription = null,
                  modifier = Modifier.size(48.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = AppStrings.get("no_alerts_created", language),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      } else {
        items(alerts, key = { it.id }) { alert ->
          CustomAlertItemCard(
            alert = alert,
            language = language,
            dateFormat = dateFormat,
            onToggle = { isEnabled -> viewModel.toggleAlert(alert, isEnabled) },
            onDelete = { viewModel.deleteAlert(alert) }
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
        .testTag("add_alert_fab"),
      containerColor = Color(0xFFC2410C),
      contentColor = Color.White
    ) {
      Icon(Icons.Default.Add, contentDescription = AppStrings.get("add_custom_alert", language))
    }
  }

  if (showAddDialog) {
    AddCustomAlertDialog(
      language = language,
      onDismiss = { showAddDialog = false },
      onConfirm = { t, type, amt, due, th, r ->
        viewModel.addAlert(t, type, amt, due, th, r)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun CustomAlertItemCard(
  alert: CustomAlertEntity,
  language: AppLanguage,
  dateFormat: SimpleDateFormat,
  onToggle: (Boolean) -> Unit,
  onDelete: () -> Unit
) {
  val (typeLabel, icon, badgeColor) = when (alert.alertType) {
    "PAYMENT_REMINDER" -> Triple(AppStrings.get("alert_type_payment", language), Icons.Default.CreditCard, Color(0xFF3B82F6))
    "BUDGET_THRESHOLD" -> Triple(AppStrings.get("alert_type_budget", language), Icons.Default.Warning, Color(0xFFF59E0B))
    "SERVICE_REMINDER" -> Triple(AppStrings.get("alert_type_service", language), Icons.Default.Speed, Color(0xFF10B981))
    else -> Triple(AppStrings.get("alert_type_general", language), Icons.Default.NotificationsActive, Color(0xFF8B5CF6))
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (alert.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
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
            color = badgeColor.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = alert.title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = typeLabel,
              style = MaterialTheme.typography.labelSmall,
              color = badgeColor,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Switch(
            checked = alert.isEnabled,
            onCheckedChange = onToggle,
            modifier = Modifier.testTag("alert_toggle_${alert.id}")
          )
          Spacer(modifier = Modifier.width(4.dp))
          IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
              Icons.Default.Delete,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Reason & Details Callout Box
      if (alert.reason.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = alert.reason,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Target amount & Due date footer
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (alert.alertType == "BUDGET_THRESHOLD") {
          Text(
            text = "${AppStrings.get("threshold_percentage", language)}: ${alert.thresholdPercentage}% (${String.format("%,.0f", alert.targetAmount)} ${AppStrings.get("currency", language)})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFF59E0B)
          )
        } else {
          Text(
            text = "${AppStrings.get("alert_due_date", language)}: ${dateFormat.format(Date(alert.dueDate))}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (alert.targetAmount > 0 && alert.alertType != "BUDGET_THRESHOLD") {
          Text(
            text = "${String.format("%,.0f", alert.targetAmount)} ${AppStrings.get("currency", language)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}
