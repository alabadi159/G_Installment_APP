package com.example.ui.dialogs

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BackupStats
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreDialog(
  viewModel: FinanceViewModel,
  language: AppLanguage,
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Export, 1: Import
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val scope = rememberCoroutineScope()

  var exportedJson by remember { mutableStateOf("") }
  var importInputText by remember { mutableStateOf("") }
  var inspectedStats by remember { mutableStateOf<BackupStats?>(null) }
  var statusMessage by remember { mutableStateOf<String?>(null) }
  var isSuccessStatus by remember { mutableStateOf(true) }

  val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale(language.code))

  LaunchedEffect(selectedTab) {
    if (selectedTab == 0) {
      exportedJson = viewModel.exportBackupJson()
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.FolderZip,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = AppStrings.get("backup_restore", language),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.primary
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = {
              selectedTab = 0
              statusMessage = null
            },
            text = { Text(AppStrings.get("export_backup", language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = {
              selectedTab = 1
              statusMessage = null
            },
            text = { Text(AppStrings.get("import_backup", language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = {
              selectedTab = 2
              statusMessage = null
            },
            text = { Text(if (language == AppLanguage.ARABIC) "تفريغ الحقول" else "Clear All", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
          )
        }

        if (selectedTab == 0) {
          // EXPORT VIEW
          Text(
            text = if (language == AppLanguage.ARABIC)
              "احفظ نسخة احتياطية محلية من كافة بياناتك (المصروفات، الدخل، الأقساط، السيارة، الذهب، السكن) كملف JSON لضمان عدم فقدانها."
            else
              "Export a full JSON backup of all your financial data (expenses, incomes, installments, car, gold, housing) locally.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "JSON Payload Preview (${exportedJson.length} bytes):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().height(120.dp)
              ) {
                Text(
                  text = exportedJson.take(500) + if (exportedJson.length > 500) "\n..." else "",
                  style = MaterialTheme.typography.bodySmall,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState())
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                clipboardManager.setText(AnnotatedString(exportedJson))
                statusMessage = AppStrings.get("copied_to_clipboard", language)
                isSuccessStatus = true
              },
              modifier = Modifier.weight(1f).testTag("copy_backup_button")
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(AppStrings.get("copy_json", language), fontSize = 12.sp)
            }

            FilledTonalButton(
              onClick = {
                val sendIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, exportedJson)
                  type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Masareef_Backup.json"))
              },
              modifier = Modifier.weight(1f).testTag("share_backup_button")
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (language == AppLanguage.ARABIC) "مشاركة الملف" else "Share JSON", fontSize = 12.sp)
            }
          }
        } else if (selectedTab == 1) {
          // IMPORT VIEW
          Text(
            text = if (language == AppLanguage.ARABIC)
              "الصق نص ملف النسخة الاحتياطية (JSON) هنا للاستعادة الفورية لقاعدة البيانات."
            else
              "Paste the backup JSON text below to inspect and restore all records.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = importInputText,
            onValueChange = {
              importInputText = it
              inspectedStats = null
              statusMessage = null
            },
            label = { Text(AppStrings.get("paste_json", language)) },
            modifier = Modifier.fillMaxWidth().height(140.dp).testTag("import_json_input"),
            maxLines = 8
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                scope.launch {
                  val stats = viewModel.inspectBackupJson(importInputText)
                  inspectedStats = stats
                  if (stats == null) {
                    statusMessage = AppStrings.get("restore_failed", language)
                    isSuccessStatus = false
                  }
                }
              },
              modifier = Modifier.weight(1f).testTag("inspect_backup_button"),
              enabled = importInputText.isNotBlank()
            ) {
              Text(if (language == AppLanguage.ARABIC) "فحص النسخة" else "Inspect", fontSize = 12.sp)
            }

            Button(
              onClick = {
                scope.launch {
                  val success = viewModel.importBackupJson(importInputText)
                  if (success) {
                    statusMessage = AppStrings.get("restore_success", language)
                    isSuccessStatus = true
                  } else {
                    statusMessage = AppStrings.get("restore_failed", language)
                    isSuccessStatus = false
                  }
                }
              },
              modifier = Modifier.weight(1f).testTag("confirm_restore_button"),
              enabled = importInputText.isNotBlank(),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
            ) {
              Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (language == AppLanguage.ARABIC) "استعادة الآن" else "Restore Now", fontSize = 12.sp)
            }
          }

          // Stats Preview Card
          inspectedStats?.let { stats ->
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "محتويات النسخة الاحتياطية (${dateFormat.format(Date(stats.exportDate))}):",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Text("• ${stats.incomesCount} مصادر دخل", style = MaterialTheme.typography.bodySmall)
                Text("• ${stats.expensesCount} مصروفات", style = MaterialTheme.typography.bodySmall)
                Text("• ${stats.installmentsCount} خطط تقسيط (تابي/تمارا/فليكس)", style = MaterialTheme.typography.bodySmall)
                Text("• ${stats.carServicesCount} سجلات صيانة سيارة", style = MaterialTheme.typography.bodySmall)
                Text("• ${stats.goldHoldingsCount} مقتنيات ذهب", style = MaterialTheme.typography.bodySmall)
                Text("• ${stats.savingsGoalsCount} أهداف ادخار", style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        } else {
          // CLEAR ALL VIEW
          Text(
            text = if (language == AppLanguage.ARABIC)
              "تفريغ جميع الحقول وبدء استخدام التطبيق كصفحة بيضاء فارغة تماماً لإدخال بياناتك الخاصة."
            else
              "Clear all records and start completely fresh with clean, empty fields.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Button(
            onClick = {
              viewModel.clearAllData()
              statusMessage = if (language == AppLanguage.ARABIC) "تم تفريغ كافة الحقول بنجاح!" else "All fields emptied successfully!"
              isSuccessStatus = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().testTag("clear_all_data_button")
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (language == AppLanguage.ARABIC) "تأكيد تفريغ كافة البيانات" else "Confirm Clear All Data")
          }
        }

        // Status banner
        statusMessage?.let { msg ->
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSuccessStatus) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                if (isSuccessStatus) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isSuccessStatus) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isSuccessStatus) Color(0xFF10B981) else Color(0xFFEF4444)
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text(if (language == AppLanguage.ARABIC) "إغلاق" else "Close")
      }
    }
  )
}
