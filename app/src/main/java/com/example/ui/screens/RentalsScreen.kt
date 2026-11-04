package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.local.entity.RentalContractEntity
import com.example.ui.dialogs.AddRentalDialog
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalsScreen(
  viewModel: FinanceViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  val language by viewModel.currentLanguage.collectAsState()
  val rentals by viewModel.rentals.collectAsState()

  var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, HOUSE, STORE, CAR, PLACE
  var showAddDialog by remember { mutableStateOf(false) }

  val filteredRentals = if (selectedTypeFilter == "ALL") {
    rentals
  } else {
    rentals.filter { it.rentalType == selectedTypeFilter }
  }

  val totalMonthlyRent = rentals.sumOf { rental ->
    when (rental.paymentFrequency) {
      "QUARTERLY" -> rental.rentAmount / 3.0
      "ANNUAL" -> rental.rentAmount / 12.0
      else -> rental.rentAmount
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Bar
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = AppStrings.get("back", language)
            )
          }
        },
        title = {
          Text(
            text = AppStrings.get("rentals_title", language),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Summary Card
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
                    colors = listOf(Color(0xFF0F766E), Color(0xFF134E4A), Color(0xFF1E293B))
                  )
                )
                .padding(20.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = AppStrings.get("total_monthly_rent", language),
                      style = MaterialTheme.typography.labelMedium,
                      color = Color(0xFFCCFBF1)
                    )
                    Text(
                      text = "${String.format("%,.0f", totalMonthlyRent)} ${AppStrings.get("currency", language)}",
                      style = MaterialTheme.typography.headlineMedium,
                      fontWeight = FontWeight.Black,
                      color = Color(0xFF6EE7B7)
                    )
                  }
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = AppStrings.get("active_rental_contracts", language),
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                      text = "${rentals.size}",
                      style = MaterialTheme.typography.titleLarge,
                      fontWeight = FontWeight.ExtraBold,
                      color = Color.White
                    )
                  }
                }
              }
            }
          }
        }

        // Filter Chips Row
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            FilterChip(
              selected = selectedTypeFilter == "ALL",
              onClick = { selectedTypeFilter = "ALL" },
              label = { Text(AppStrings.get("rental_type_all", language), fontSize = 11.sp) }
            )
            FilterChip(
              selected = selectedTypeFilter == "HOUSE",
              onClick = { selectedTypeFilter = "HOUSE" },
              label = { Text(AppStrings.get("rental_type_house", language), fontSize = 11.sp) }
            )
            FilterChip(
              selected = selectedTypeFilter == "STORE",
              onClick = { selectedTypeFilter = "STORE" },
              label = { Text(AppStrings.get("rental_type_store", language), fontSize = 11.sp) }
            )
            FilterChip(
              selected = selectedTypeFilter == "CAR",
              onClick = { selectedTypeFilter = "CAR" },
              label = { Text(AppStrings.get("rental_type_car", language), fontSize = 11.sp) }
            )
            FilterChip(
              selected = selectedTypeFilter == "PLACE",
              onClick = { selectedTypeFilter = "PLACE" },
              label = { Text(AppStrings.get("rental_type_place", language), fontSize = 11.sp) }
            )
          }
        }

        // Rental Items
        if (filteredRentals.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  modifier = Modifier.size(64.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.Business,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(32.dp)
                    )
                  }
                }
                Text(
                  text = AppStrings.get("no_rentals_recorded", language),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                  onClick = { showAddDialog = true },
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(AppStrings.get("add_rental", language))
                }
              }
            }
          }
        } else {
          items(filteredRentals) { rental ->
            RentalItemCard(
              rental = rental,
              language = language,
              onDelete = { viewModel.deleteRental(rental) }
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
        .testTag("add_rental_fab"),
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
      Icon(Icons.Default.Add, contentDescription = AppStrings.get("add_rental", language))
    }
  }

  if (showAddDialog) {
    AddRentalDialog(
      language = language,
      onDismiss = { showAddDialog = false },
      onConfirm = { title, type, amount, freq, water, elec, duration, plate, notes ->
        viewModel.addRental(title, type, amount, freq, water, elec, duration, plate, notes)
        showAddDialog = false
      }
    )
  }
}

@Composable
private fun RentalItemCard(
  rental: RentalContractEntity,
  language: AppLanguage,
  onDelete: () -> Unit
) {
  val icon: ImageVector = when (rental.rentalType) {
    "HOUSE" -> Icons.Default.Home
    "STORE" -> Icons.Default.ShoppingBag
    "CAR" -> Icons.Default.DirectionsCar
    else -> Icons.Default.Place
  }

  val typeColor = when (rental.rentalType) {
    "HOUSE" -> Color(0xFF0D9488)
    "STORE" -> Color(0xFFD97706)
    "CAR" -> Color(0xFF2563EB)
    else -> Color(0xFF7C3AED)
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
          color = typeColor.copy(alpha = 0.12f),
          modifier = Modifier.size(44.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = typeColor,
              modifier = Modifier.size(22.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = rental.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = typeColor.copy(alpha = 0.1f)
            ) {
              Text(
                text = when (rental.rentalType) {
                  "HOUSE" -> AppStrings.get("rental_type_house", language)
                  "STORE" -> AppStrings.get("rental_type_store", language)
                  "CAR" -> AppStrings.get("rental_type_car", language)
                  else -> AppStrings.get("rental_type_place", language)
                },
                style = MaterialTheme.typography.labelSmall,
                color = typeColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )
            }

            if (rental.isWaterIncluded && rental.isElectricityIncluded) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF10B981).copy(alpha = 0.12f)
              ) {
                Text(
                  text = AppStrings.get("water_electricity_included", language),
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF059669),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  fontSize = 10.sp
                )
              }
            }
          }

          if (rental.locationOrPlate.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = rental.locationOrPlate,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${String.format("%,.0f", rental.rentAmount)} ${AppStrings.get("currency", language)}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = when (rental.paymentFrequency) {
            "QUARTERLY" -> if (language == AppLanguage.ARABIC) "/ كل 3 أشهر" else "/ Quarter"
            "ANNUAL" -> if (language == AppLanguage.ARABIC) "/ سنوي" else "/ Year"
            else -> if (language == AppLanguage.ARABIC) "/ شهري" else "/ Month"
          },
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = AppStrings.get("delete", language),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
