package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val amount: Double,
  val category: String, // FOOD, SHOPPING, TRANSPORT, BILLS, HEALTH, ENTERTAINMENT, OTHER
  val date: Long = System.currentTimeMillis(),
  val isMonthlyRecurring: Boolean = false,
  val notes: String = ""
)

@Entity(tableName = "incomes")
data class IncomeEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val amount: Double,
  val category: String, // SALARY, FREELANCE, INVESTMENTS, RENTAL, BONUS, BUSINESS, OTHER
  val receiptDate: Long = System.currentTimeMillis(),
  val isRecurringMonthly: Boolean = true,
  val notes: String = ""
)

@Entity(tableName = "custom_alerts")
data class CustomAlertEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val alertType: String, // PAYMENT_REMINDER, BUDGET_THRESHOLD, SERVICE_REMINDER, GENERAL
  val targetAmount: Double = 0.0,
  val dueDate: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000),
  val thresholdPercentage: Int = 80, // e.g. 80% or 100% of budget limit
  val reason: String = "",
  val isEnabled: Boolean = true,
  val isTriggered: Boolean = false
)

@Entity(tableName = "installments")
data class InstallmentPlanEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val provider: String, // "TABBY", "TAMARA", "SNB_FLEX", "OTHER"
  val totalAmount: Double,
  val monthsCount: Int, // Tabby & Tamara: 2-24, SNB_FLEX: 4
  val monthlyAmount: Double, // totalAmount / monthsCount
  val startDate: Long = System.currentTimeMillis(),
  val paidMonthsCount: Int = 0,
  val status: String = "ACTIVE", // "NEW", "ACTIVE", "COMPLETED"
  val notes: String = ""
)

@Entity(tableName = "car_loan")
data class CarLoanEntity(
  @PrimaryKey val id: Long = 1, // Single active car loan record
  val carName: String = "Toyota Camry 2024",
  val totalCarPrice: Double = 120000.0,
  val downPayment: Double = 15000.0,
  val termMonths: Int = 60, // Default 60 months
  val annualProfitRate: Double = 3.5, // 3.5%
  val balloonPayment: Double = 35000.0, // Last payment / دفعة التملك
  val monthlyInstallment: Double = 1680.0,
  val startDate: Long = System.currentTimeMillis(),
  val paidMonths: Int = 12,
  val balloonRefinanced: Boolean = false, // Has user opted to split balloon payment?
  val balloonBankName: String = "Al Rajhi Bank",
  val balloonTermMonths: Int = 24, // 12-36 months
  val balloonMonthlyAmount: Double = 1550.0
)

@Entity(tableName = "car_services")
data class CarServiceEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val serviceType: String, // OIL_FILTER, BRAKES, TIRES, INSPECTION, INSURANCE, REGISTRATION, BATTERY, GENERAL
  val cost: Double,
  val date: Long = System.currentTimeMillis(),
  val currentOdometerKm: Int = 25000,
  val nextDueOdometerKm: Int = 35000,
  val nextDueDate: Long = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000),
  val notes: String = ""
)

@Entity(tableName = "gold_holdings")
data class GoldHoldingEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val itemType: String, // BULLION_24K, COIN_21K, JEWELRY_21K, JEWELRY_18K
  val karat: Int = 24, // 24, 22, 21, 18
  val weightGrams: Double,
  val purchasePricePerGram: Double,
  val purchaseDate: Long = System.currentTimeMillis(),
  val notes: String = ""
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val targetAmount: Double,
  val currentAmount: Double = 0.0,
  val deadlineDate: Long = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000),
  val category: String = "EMERGENCY_FUND",
  val notes: String = ""
)

@Entity(tableName = "telecom_contracts")
data class TelecomContractEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val providerName: String, // STC, Mobily, Zain, Salam, etc.
  val planMonthlyFee: Double, // Postpaid plan monthly fee
  val hasDeviceCommitment: Boolean = false,
  val deviceName: String = "",
  val deviceTotalCost: Double = 0.0,
  val deviceCommitmentMonths: Int = 24,
  val deviceMonthlyCost: Double = 0.0, // deviceTotalCost / deviceCommitmentMonths
  val startDate: Long = System.currentTimeMillis(),
  val paidMonths: Int = 0,
  val contractCount: Int = 1,
  val notes: String = ""
)

@Entity(tableName = "housing_expenses")
data class HousingEntity(
  @PrimaryKey val id: Long = 1, // Single active housing profile
  val propertyName: String = "المنزل الرئيسي / Apartment",
  val rentAmount: Double = 2500.0, // Monthly or converted monthly
  val paymentFrequency: String = "MONTHLY", // MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL
  val waterAmount: Double = 120.0,
  val isWaterIncluded: Boolean = true, // Inclusive of water
  val electricityAmount: Double = 350.0,
  val isElectricityIncluded: Boolean = true, // Inclusive of electricity
  val nextDueDate: Long = System.currentTimeMillis() + (20L * 24 * 60 * 60 * 1000),
  val notes: String = ""
)

@Entity(tableName = "rental_contracts")
data class RentalContractEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val rentalType: String, // "HOUSE", "STORE", "CAR", "PLACE"
  val rentAmount: Double,
  val paymentFrequency: String = "MONTHLY", // "MONTHLY", "QUARTERLY", "SEMI_ANNUAL", "ANNUAL"
  val isWaterIncluded: Boolean = false,
  val isElectricityIncluded: Boolean = false,
  val startDate: Long = System.currentTimeMillis(),
  val durationMonths: Int = 12,
  val paidMonths: Int = 0,
  val nextDueDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
  val locationOrPlate: String = "",
  val notes: String = ""
)

@Entity(tableName = "monthly_budget")
data class MonthlyBudgetEntity(
  @PrimaryKey val id: Long = 1,
  val targetAmount: Double = 0.0,
  val warningThresholdPercent: Int = 80,
  val isAlertEnabled: Boolean = true
)
