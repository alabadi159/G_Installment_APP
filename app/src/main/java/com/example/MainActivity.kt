package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Translate
import com.example.ui.dialogs.BackupRestoreDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.CarScreen
import com.example.ui.screens.CommitmentsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.IncomeScreen
import com.example.ui.screens.InstallmentsScreen
import com.example.ui.screens.RentalsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.WalletsScreen
import com.example.ui.theme.MasareefTheme
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: FinanceViewModel = viewModel()
      val isDarkMode by viewModel.isDarkMode.collectAsState()
      val language by viewModel.currentLanguage.collectAsState()

      MasareefTheme(darkTheme = isDarkMode) {
        val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
          MainAppContent(viewModel = viewModel)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: FinanceViewModel) {
  val language by viewModel.currentLanguage.collectAsState()
  val isDarkMode by viewModel.isDarkMode.collectAsState()
  val isSyncing by viewModel.isSyncing.collectAsState()
  val alerts by viewModel.alerts.collectAsState()

  var selectedNavigationIndex by remember { mutableIntStateOf(0) }
  var isShowingAlertsScreen by remember { mutableStateOf(false) }
  var isShowingRentalsScreen by remember { mutableStateOf(false) }
  var showBackupRestoreDialog by remember { mutableStateOf(false) }
  var showCloudSyncDialog by remember { mutableStateOf(false) }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val activeAlertsCount = alerts.count { it.isEnabled }

  val navItems = listOf(
    NavigationItem(
      titleKey = "dashboard",
      icon = Icons.Default.Home,
      testTag = "nav_dashboard"
    ),
    NavigationItem(
      titleKey = "income",
      icon = Icons.Default.MonetizationOn,
      testTag = "nav_income"
    ),
    NavigationItem(
      titleKey = "installments",
      icon = Icons.Default.CreditCard,
      testTag = "nav_installments"
    ),
    NavigationItem(
      titleKey = "car_section",
      icon = Icons.Default.DirectionsCar,
      testTag = "nav_car"
    ),
    NavigationItem(
      titleKey = "wallets",
      icon = Icons.Default.Savings,
      testTag = "nav_wallets"
    ),
    NavigationItem(
      titleKey = "reports",
      icon = Icons.Default.BarChart,
      testTag = "nav_reports"
    )
  )

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        navigationIcon = {
          if (isShowingAlertsScreen || isShowingRentalsScreen) {
            IconButton(onClick = {
              isShowingAlertsScreen = false
              isShowingRentalsScreen = false
            }) {
              Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = when {
                isShowingAlertsScreen -> AppStrings.get("alerts_and_reminders", language)
                isShowingRentalsScreen -> AppStrings.get("rentals_title", language)
                else -> AppStrings.get("app_name", language)
              },
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        },
        actions = {
          // Alerts & Reminders Center Button with Badge
          IconButton(
            onClick = { isShowingAlertsScreen = !isShowingAlertsScreen },
            modifier = Modifier.testTag("alerts_action_button")
          ) {
            BadgedBox(
              badge = {
                if (activeAlertsCount > 0) {
                  Badge(containerColor = Color(0xFFEF4444)) {
                    Text("$activeAlertsCount", color = Color.White, fontSize = 9.sp)
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = AppStrings.get("alerts", language),
                tint = if (isShowingAlertsScreen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Backup & Restore (JSON)
          IconButton(
            onClick = { showBackupRestoreDialog = true },
            modifier = Modifier.testTag("backup_action_button")
          ) {
            Icon(
              imageVector = Icons.Default.FolderZip,
              contentDescription = AppStrings.get("backup_restore", language),
              tint = MaterialTheme.colorScheme.primary
            )
          }

          // Cloud Sync / Firebase button
          IconButton(
            onClick = { showCloudSyncDialog = true },
            modifier = Modifier.testTag("sync_action_button")
          ) {
            if (isSyncing) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
              )
            } else {
              Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = AppStrings.get("cloud_sync", language),
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }

          // Language Toggle Button (AR / EN)
          IconButton(
            onClick = { viewModel.toggleLanguage() },
            modifier = Modifier.testTag("language_toggle_button")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = AppStrings.get("language", language),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = if (language == AppLanguage.ARABIC) "EN" else "عربي",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }

          // Dark / Light Theme Toggle
          IconButton(
            onClick = { viewModel.toggleTheme() },
            modifier = Modifier.testTag("theme_toggle_button")
          ) {
            Icon(
              imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = AppStrings.get("theme_mode", language)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface
        )
      )
    },
    bottomBar = {
      if (!isShowingAlertsScreen && !isShowingRentalsScreen) {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
          navItems.forEachIndexed { index, item ->
            NavigationBarItem(
              selected = selectedNavigationIndex == index,
              onClick = { selectedNavigationIndex = index },
              icon = { Icon(item.icon, contentDescription = AppStrings.get(item.titleKey, language)) },
              label = {
                Text(
                  text = AppStrings.get(item.titleKey, language),
                  fontSize = 10.sp,
                  fontWeight = if (selectedNavigationIndex == index) FontWeight.Bold else FontWeight.Normal,
                  maxLines = 1
                )
              },
              modifier = Modifier.testTag(item.testTag)
            )
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (isShowingAlertsScreen) {
        AlertsScreen(viewModel = viewModel, onDismiss = { isShowingAlertsScreen = false })
      } else if (isShowingRentalsScreen) {
        RentalsScreen(viewModel = viewModel, onBack = { isShowingRentalsScreen = false })
      } else {
        AnimatedContent(targetState = selectedNavigationIndex, label = "screen_transition") { screenIndex ->
          when (screenIndex) {
            0 -> DashboardScreen(
              viewModel = viewModel,
              onNavigateToIncome = { selectedNavigationIndex = 1 },
              onNavigateToInstallments = { selectedNavigationIndex = 2 },
              onNavigateToCar = { selectedNavigationIndex = 3 },
              onNavigateToWallets = { selectedNavigationIndex = 4 },
              onNavigateToCommitments = { selectedNavigationIndex = 5 },
              onNavigateToReports = { selectedNavigationIndex = 5 },
              onNavigateToRentals = { isShowingRentalsScreen = true }
            )
            1 -> IncomeScreen(viewModel = viewModel)
            2 -> InstallmentsScreen(viewModel = viewModel)
            3 -> CarScreen(viewModel = viewModel)
            4 -> WalletsScreen(viewModel = viewModel)
            5 -> ReportsScreen(viewModel = viewModel)
          }
        }
      }
    }
  }

  if (showBackupRestoreDialog) {
    BackupRestoreDialog(
      viewModel = viewModel,
      language = language,
      onDismiss = { showBackupRestoreDialog = false }
    )
  }

  if (showCloudSyncDialog) {
    com.example.ui.dialogs.CloudSyncDialog(
      viewModel = viewModel,
      language = language,
      onDismiss = { showCloudSyncDialog = false }
    )
  }
}

data class NavigationItem(
  val titleKey: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector,
  val testTag: String
)
