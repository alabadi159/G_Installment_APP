package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CarLoanEntity
import com.example.data.local.entity.CarServiceEntity
import com.example.data.local.entity.CustomAlertEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.GoldHoldingEntity
import com.example.data.local.entity.HousingEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.InstallmentPlanEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TelecomContractEntity
import com.example.data.remote.GoldRates
import com.example.data.repository.FinanceRepository
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import android.content.Context
import com.example.data.remote.AuthManager
import com.example.data.remote.FirestoreManager
import com.google.firebase.auth.FirebaseUser

data class FinancialOverview(
  val totalMonthlyIncome: Double,
  val totalAllTimeIncome: Double,
  val netMonthlyCashflow: Double,
  val savingsRatePercentage: Double,
  val totalMonthlyObligations: Double,
  val monthlyInstallmentsTotal: Double,
  val carMonthlyTotal: Double,
  val telecomMonthlyTotal: Double,
  val housingMonthlyTotal: Double,
  val currentMonthExpensesTotal: Double,
  val totalSavingsGoldValue: Double,
  val totalGoldWeightGrams: Double,
  val totalGoldCurrentValue: Double,
  val totalGoldProfitLoss: Double
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
  val firestoreManager = FirestoreManager(application)
  val authManager = AuthManager()
  private val database = AppDatabase.getDatabase(application, viewModelScope)
  private val repository = FinanceRepository(database.appDao(), firestoreManager = firestoreManager)

  // Auth & Cloud Sync State
  val authUser: StateFlow<FirebaseUser?> = authManager.authStateFlow().stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), authManager.currentUser
  )

  private val _isCloudSyncing = MutableStateFlow(false)
  val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

  // App Settings State
  private val _currentLanguage = MutableStateFlow(AppLanguage.ARABIC)
  val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

  private val _isDarkMode = MutableStateFlow(true) // Modern sleek dark mode default
  val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  // Room Flows
  val expenses = repository.allExpenses.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val incomes = repository.allIncomes.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val alerts = repository.allAlerts.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val installments = repository.allInstallments.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val carLoan = repository.carLoan.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), null
  )

  val carServices = repository.allCarServices.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val goldHoldings = repository.allGoldHoldings.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val savingsGoals = repository.allSavingsGoals.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val telecomContracts = repository.allTelecomContracts.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
  )

  val housing = repository.housing.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), null
  )

  val goldRates: StateFlow<GoldRates> = repository.goldRates

  init {
    viewModelScope.launch {
      repository.refreshGoldPrices()
    }
  }

  // Combined Financial Overview
  val overview: StateFlow<FinancialOverview> = combine(
    incomes, installments, carLoan, telecomContracts, housing, expenses, goldHoldings, savingsGoals, goldRates
  ) { args: Array<Any?> ->
    @Suppress("UNCHECKED_CAST")
    val incList = args[0] as List<IncomeEntity>
    @Suppress("UNCHECKED_CAST")
    val instList = args[1] as List<InstallmentPlanEntity>
    val car = args[2] as? CarLoanEntity
    @Suppress("UNCHECKED_CAST")
    val telecomList = args[3] as List<TelecomContractEntity>
    val house = args[4] as? HousingEntity
    @Suppress("UNCHECKED_CAST")
    val expList = args[5] as List<ExpenseEntity>
    @Suppress("UNCHECKED_CAST")
    val goldList = args[6] as List<GoldHoldingEntity>
    @Suppress("UNCHECKED_CAST")
    val savingsList = args[7] as List<SavingsGoalEntity>
    val rates = args[8] as GoldRates

    // Income calculations
    val monthlyIncomeTotal = incList.filter { it.isRecurringMonthly }.sumOf { it.amount }
    val allTimeIncomeTotal = incList.sumOf { it.amount }

    // Active installments monthly total
    val activeInstallmentsMonthly = instList
      .filter { it.status == "ACTIVE" }
      .sumOf { it.monthlyAmount }

    // Car monthly: regular monthly + balloon monthly if refinanced
    val carMonthly = if (car != null) {
      if (car.balloonRefinanced) {
        car.monthlyInstallment + car.balloonMonthlyAmount
      } else {
        car.monthlyInstallment
      }
    } else 0.0

    // Telecom monthly: plan fee * count + device installment
    val telecomMonthly = telecomList.sumOf { item ->
      (item.planMonthlyFee * item.contractCount) + item.deviceMonthlyCost
    }

    // Housing effective monthly:
    val housingMonthly = if (house != null) {
      val baseRentMonthly = when (house.paymentFrequency) {
        "QUARTERLY" -> house.rentAmount / 3.0
        "SEMI_ANNUAL" -> house.rentAmount / 6.0
        "ANNUAL" -> house.rentAmount / 12.0
        else -> house.rentAmount
      }
      val extraWater = if (!house.isWaterIncluded) house.waterAmount else 0.0
      val extraElec = if (!house.isElectricityIncluded) house.electricityAmount else 0.0
      baseRentMonthly + extraWater + extraElec
    } else 0.0

    val totalObligations = activeInstallmentsMonthly + carMonthly + telecomMonthly + housingMonthly
    val currentMonthExpenses = expList.sumOf { it.amount }

    // Net Monthly Cashflow & Savings Rate
    val totalMonthlyOutflow = totalObligations + currentMonthExpenses
    val netCashflow = monthlyIncomeTotal - totalMonthlyOutflow
    val savingsRate = if (monthlyIncomeTotal > 0) {
      ((netCashflow.coerceAtLeast(0.0) / monthlyIncomeTotal) * 100.0)
    } else 0.0

    // Gold Portfolio computations
    var totalGoldWeight = 0.0
    var totalGoldCurrentVal = 0.0
    var totalGoldCost = 0.0

    goldList.forEach { holding ->
      totalGoldWeight += holding.weightGrams
      val currentPricePerGram = when (holding.karat) {
        24 -> rates.price24kPerGram
        22 -> rates.price22kPerGram
        21 -> rates.price21kPerGram
        18 -> rates.price18kPerGram
        else -> rates.price24kPerGram
      }
      totalGoldCurrentVal += holding.weightGrams * currentPricePerGram
      totalGoldCost += holding.weightGrams * holding.purchasePricePerGram
    }

    val totalSavingsVal = savingsList.sumOf { it.currentAmount }
    val totalSavingsAndGold = totalSavingsVal + totalGoldCurrentVal

    FinancialOverview(
      totalMonthlyIncome = Math.round(monthlyIncomeTotal * 100.0) / 100.0,
      totalAllTimeIncome = Math.round(allTimeIncomeTotal * 100.0) / 100.0,
      netMonthlyCashflow = Math.round(netCashflow * 100.0) / 100.0,
      savingsRatePercentage = Math.round(savingsRate * 10.0) / 10.0,
      totalMonthlyObligations = Math.round(totalObligations * 100.0) / 100.0,
      monthlyInstallmentsTotal = Math.round(activeInstallmentsMonthly * 100.0) / 100.0,
      carMonthlyTotal = Math.round(carMonthly * 100.0) / 100.0,
      telecomMonthlyTotal = Math.round(telecomMonthly * 100.0) / 100.0,
      housingMonthlyTotal = Math.round(housingMonthly * 100.0) / 100.0,
      currentMonthExpensesTotal = Math.round(currentMonthExpenses * 100.0) / 100.0,
      totalSavingsGoldValue = Math.round(totalSavingsAndGold * 100.0) / 100.0,
      totalGoldWeightGrams = Math.round(totalGoldWeight * 100.0) / 100.0,
      totalGoldCurrentValue = Math.round(totalGoldCurrentVal * 100.0) / 100.0,
      totalGoldProfitLoss = Math.round((totalGoldCurrentVal - totalGoldCost) * 100.0) / 100.0
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    FinancialOverview(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
  )

  // Actions
  fun toggleLanguage() {
    _currentLanguage.value = if (_currentLanguage.value == AppLanguage.ARABIC) {
      AppLanguage.ENGLISH
    } else {
      AppLanguage.ARABIC
    }
  }

  fun toggleTheme() {
    _isDarkMode.value = !_isDarkMode.value
  }

  fun triggerSync() {
    viewModelScope.launch {
      _isSyncing.value = true
      repository.refreshGoldPrices()
      kotlinx.coroutines.delay(1200)
      _isSyncing.value = false
    }
  }

  fun refreshGoldPrices() {
    viewModelScope.launch {
      repository.refreshGoldPrices()
    }
  }

  // Incomes
  fun addIncome(
    title: String,
    amount: Double,
    category: String,
    receiptDate: Long = System.currentTimeMillis(),
    isRecurringMonthly: Boolean = true,
    notes: String = ""
  ) {
    viewModelScope.launch {
      repository.addIncome(title, amount, category, receiptDate, isRecurringMonthly, notes)
    }
  }

  fun deleteIncome(income: IncomeEntity) {
    viewModelScope.launch { repository.deleteIncome(income) }
  }

  // Alerts
  fun addAlert(
    title: String,
    alertType: String,
    targetAmount: Double,
    dueDate: Long,
    thresholdPercentage: Int,
    reason: String
  ) {
    viewModelScope.launch {
      repository.addAlert(title, alertType, targetAmount, dueDate, thresholdPercentage, reason)
    }
  }

  fun toggleAlert(alert: CustomAlertEntity, isEnabled: Boolean) {
    viewModelScope.launch { repository.toggleAlert(alert, isEnabled) }
  }

  fun deleteAlert(alert: CustomAlertEntity) {
    viewModelScope.launch { repository.deleteAlert(alert) }
  }

  // Expenses
  fun addExpense(title: String, amount: Double, category: String, isMonthly: Boolean, notes: String = "") {
    viewModelScope.launch {
      repository.addExpense(
        ExpenseEntity(
          title = title,
          amount = amount,
          category = category,
          isMonthlyRecurring = isMonthly,
          notes = notes
        )
      )
    }
  }

  fun deleteExpense(expense: ExpenseEntity) {
    viewModelScope.launch { repository.deleteExpense(expense) }
  }

  // Installments
  fun addInstallment(
    title: String,
    provider: String,
    totalAmount: Double,
    monthsCount: Int,
    startDate: Long,
    notes: String = "",
    paidMonthsCount: Int = 0
  ) {
    viewModelScope.launch {
      repository.addInstallment(title, provider, totalAmount, monthsCount, startDate, notes, paidMonthsCount)
    }
  }

  fun markInstallmentMonthPaid(installment: InstallmentPlanEntity) {
    viewModelScope.launch {
      repository.incrementPaidInstallment(installment)
    }
  }

  fun deleteInstallment(installment: InstallmentPlanEntity) {
    viewModelScope.launch {
      repository.deleteInstallment(installment)
    }
  }

  fun updateInstallment(
    id: Long,
    title: String,
    provider: String,
    totalAmount: Double,
    monthsCount: Int,
    startDate: Long,
    paidMonthsCount: Int,
    notes: String = ""
  ) {
    viewModelScope.launch {
      repository.updateInstallment(id, title, provider, totalAmount, monthsCount, startDate, paidMonthsCount, notes)
    }
  }

  // Backup & Restore
  suspend fun exportBackupJson(): String {
    return repository.backupManager.exportToJson()
  }

  suspend fun inspectBackupJson(jsonStr: String): com.example.data.local.BackupStats? {
    return repository.backupManager.inspectBackup(jsonStr)
  }

  suspend fun importBackupJson(jsonStr: String): Boolean {
    val success = repository.backupManager.importFromJson(jsonStr)
    if (success) {
      repository.refreshGoldPrices()
    }
    return success
  }

  // Car
  fun updateCarLoan(
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
  ) {
    viewModelScope.launch {
      repository.updateCarLoan(
        carName, totalPrice, downPayment, termMonths, annualProfitRate, balloonPayment,
        startDate, paidMonths, balloonRefinanced, balloonBankName, balloonTermMonths
      )
    }
  }

  fun addCarService(
    serviceType: String,
    cost: Double,
    currentOdo: Int,
    nextDueOdo: Int,
    nextDueDate: Long,
    notes: String
  ) {
    viewModelScope.launch {
      repository.addCarService(
        CarServiceEntity(
          serviceType = serviceType,
          cost = cost,
          currentOdometerKm = currentOdo,
          nextDueOdometerKm = nextDueOdo,
          nextDueDate = nextDueDate,
          notes = notes
        )
      )
    }
  }

  fun deleteCarService(service: CarServiceEntity) {
    viewModelScope.launch { repository.deleteCarService(service) }
  }

  // Gold
  fun addGoldHolding(
    itemType: String,
    karat: Int,
    weightGrams: Double,
    purchasePricePerGram: Double,
    purchaseDate: Long,
    notes: String = ""
  ) {
    viewModelScope.launch {
      repository.addGoldHolding(itemType, karat, weightGrams, purchasePricePerGram, purchaseDate, notes)
    }
  }

  fun deleteGoldHolding(holding: GoldHoldingEntity) {
    viewModelScope.launch { repository.deleteGoldHolding(holding) }
  }

  // Savings
  fun addSavingsGoal(title: String, targetAmount: Double, currentAmount: Double, deadline: Long, category: String) {
    viewModelScope.launch {
      repository.addSavingsGoal(
        SavingsGoalEntity(
          title = title,
          targetAmount = targetAmount,
          currentAmount = currentAmount,
          deadlineDate = deadline,
          category = category
        )
      )
    }
  }

  fun depositToSavingsGoal(goal: SavingsGoalEntity, amount: Double) {
    viewModelScope.launch {
      repository.depositToSavingsGoal(goal, amount)
    }
  }

  fun deleteSavingsGoal(goal: SavingsGoalEntity) {
    viewModelScope.launch { repository.deleteSavingsGoal(goal) }
  }

  // Telecom
  fun addTelecomContract(
    providerName: String,
    planMonthlyFee: Double,
    hasDeviceCommitment: Boolean,
    deviceName: String,
    deviceTotalCost: Double,
    deviceCommitmentMonths: Int,
    contractCount: Int,
    startDate: Long,
    notes: String = ""
  ) {
    viewModelScope.launch {
      repository.addTelecomContract(
        providerName, planMonthlyFee, hasDeviceCommitment, deviceName,
        deviceTotalCost, deviceCommitmentMonths, contractCount, startDate, notes
      )
    }
  }

  fun deleteTelecomContract(contract: TelecomContractEntity) {
    viewModelScope.launch { repository.deleteTelecomContract(contract) }
  }

  // Housing
  fun saveHousing(
    propertyName: String,
    rentAmount: Double,
    paymentFrequency: String,
    waterAmount: Double,
    isWaterIncluded: Boolean,
    electricityAmount: Double,
    isElectricityIncluded: Boolean,
    nextDueDate: Long,
    notes: String = ""
  ) {
    viewModelScope.launch {
      repository.saveHousing(
        propertyName, rentAmount, paymentFrequency, waterAmount, isWaterIncluded,
        electricityAmount, isElectricityIncluded, nextDueDate, notes
      )
    }
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllData()
    }
  }

  // Cloud Sync & Auth Operations
  fun signInWithGoogle(context: Context, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      _isCloudSyncing.value = true
      val res = authManager.signInWithGoogle(context)
      _isCloudSyncing.value = false
      if (res.isSuccess) {
        val user = res.getOrNull()
        val email = user?.email ?: "Google Account"
        onResult(true, email)
        // Automatically fetch cloud data if available
        user?.uid?.let { uid ->
          repository.syncAllFromCloud(uid)
        }
      } else {
        onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Sign-in failed")
      }
    }
  }

  fun signOut() {
    authManager.signOut()
  }

  fun syncToCloud(onComplete: (Boolean, String) -> Unit) {
    val uid = authManager.currentUser?.uid
    if (uid.isNullOrBlank()) {
      onComplete(false, "يجب تسجيل الدخول بحساب Google أولاً للمزامنة السحابية")
      return
    }
    viewModelScope.launch {
      _isCloudSyncing.value = true
      val res = repository.syncAllToCloud(uid)
      _isCloudSyncing.value = false
      if (res.isSuccess) {
        onComplete(true, "تم رفع ومزامنة جميع البيانات بنجاح إلى Firebase")
      } else {
        onComplete(false, res.exceptionOrNull()?.localizedMessage ?: "حدث خطأ أثناء المزامنة")
      }
    }
  }

  fun syncFromCloud(onComplete: (Boolean, String) -> Unit) {
    val uid = authManager.currentUser?.uid
    if (uid.isNullOrBlank()) {
      onComplete(false, "يجب تسجيل الدخول بحساب Google أولاً للمزامنة السحابية")
      return
    }
    viewModelScope.launch {
      _isCloudSyncing.value = true
      val res = repository.syncAllFromCloud(uid)
      _isCloudSyncing.value = false
      if (res.isSuccess) {
        val count = res.getOrDefault(0)
        onComplete(true, "تم تنزيل وتحديث $count سجل من Firebase بنجاح")
      } else {
        onComplete(false, res.exceptionOrNull()?.localizedMessage ?: "حدث خطأ أثناء الاسترجاع")
      }
    }
  }
}

