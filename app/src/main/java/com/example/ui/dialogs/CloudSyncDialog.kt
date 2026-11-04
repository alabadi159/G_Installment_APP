package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun CloudSyncDialog(
  viewModel: FinanceViewModel,
  language: AppLanguage,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val authUser by viewModel.authUser.collectAsState()
  val isSyncing by viewModel.isCloudSyncing.collectAsState()
  var operationStatus by remember { mutableStateOf<String?>(null) }
  var isStatusSuccess by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = { if (!isSyncing) onDismiss() },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(Color(0xFFF59E0B).copy(alpha = 0.2f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CloudDone,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = AppStrings.get("cloud_sync", language),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = AppStrings.get("cloud_sync_desc", language),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val user = authUser
        if (user != null) {
          // Connected Account Card
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.AccountCircle,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(28.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = AppStrings.get("signed_in_as", language),
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = user.email ?: user.displayName ?: "Firebase User",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          // Push & Pull Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                operationStatus = null
                viewModel.syncToCloud { success, message ->
                  isStatusSuccess = success
                  operationStatus = message
                  Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
              },
              enabled = !isSyncing,
              modifier = Modifier
                .weight(1f)
                .testTag("upload_to_cloud_button"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(AppStrings.get("upload_to_cloud", language), fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = {
                operationStatus = null
                viewModel.syncFromCloud { success, message ->
                  isStatusSuccess = success
                  operationStatus = message
                  Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
              },
              enabled = !isSyncing,
              modifier = Modifier
                .weight(1f)
                .testTag("download_from_cloud_button"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(AppStrings.get("download_from_cloud", language), fontSize = 12.sp)
            }
          }

          // Sign Out Button
          OutlinedButton(
            onClick = {
              viewModel.signOut()
              operationStatus = null
            },
            enabled = !isSyncing,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("sign_out_button"),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(AppStrings.get("sign_out", language))
          }
        } else {
          // Unauthenticated state: Google Sign-In Card
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = if (language == AppLanguage.ARABIC) {
                  "قم بتسجيل الدخول لربط مصاريفك وأقساطك بقاعدة بيانات Firestore السحابية واسترجاعها في أي وقت."
                } else {
                  "Sign in to link your expenses, installments, and savings to Firestore Cloud database."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(12.dp))

              Button(
                onClick = {
                  operationStatus = null
                  viewModel.signInWithGoogle(context) { success, msg ->
                    isStatusSuccess = success
                    operationStatus = if (success) "تم تسجيل الدخول: $msg" else "فشل الدخول: $msg"
                  }
                },
                enabled = !isSyncing,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("google_sign_in_button"),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = AppStrings.get("sign_in_with_google", language),
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        // Loading or Status
        AnimatedVisibility(visible = isSyncing) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (language == AppLanguage.ARABIC) "جارٍ الاتصال بالسحابة..." else "Connecting to cloud...",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        operationStatus?.let { status ->
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isStatusSuccess) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = status,
              modifier = Modifier.padding(10.dp),
              fontSize = 12.sp,
              color = if (isStatusSuccess) Color(0xFF047857) else MaterialTheme.colorScheme.error
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = onDismiss,
        enabled = !isSyncing
      ) {
        Text(AppStrings.get("cancel", language))
      }
    }
  )
}
