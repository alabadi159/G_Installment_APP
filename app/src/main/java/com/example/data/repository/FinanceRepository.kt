package com.example.data.repository

import com.example.data.local.dao.AppDao
import com.example.data.local.entity.CarLoanEntity
import com.example.data.local.entity.CarServiceEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.GoldHoldingEntity
import com.example.data.local.entity.HousingEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.InstallmentPlanEntity
import com.example.data.local.entity.MonthlyBudgetEntity
import com.example.data.local.entity.RentalContractEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TelecomContractEntity
import com.example.data.remote.GoldPriceService
import com.example.data.remote.GoldRates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.example.data.remote.FirestoreManager
import com.example.ui.components.MonthBreakdownItem
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FinanceRepository(
  private val appDao: AppDao,
  private val goldPriceService: GoldPriceService = GoldPriceService(),
  private val firestoreManager: FirestoreManager? = null
) {
  // Flows from Room Database
  val allExpenses: Flow<List<ExpenseEntity>> = appDao.getAllExpenses()
  val allIncomes: Flow<List<com.example.data.local.entity.IncomeEntity>> = appDao.getAllIncomes()
  val allAlerts: Flow<List<com.example.data.local.entity.CustomAlertEntity>> = appDao.getAllAlerts()
  val allInstallments: Flow<List<InstallmentPlanEntity>> = appDao.getAllInstallments()
  val carLoan: Flow<CarLoanEntity?> = appDao.getCarLoan()
  val allCarServices: Flow<List<CarServiceEntity>> = appDao.getAllCarServices()
  val allGoldHoldings: Flow<List<GoldHoldingEntity>> = appDao.getAllGoldHoldings()
  val allSavingsGoals: Flow<List<SavingsGoalEntity>> = appDao.getAllSavingsGoals()
  val allTelecomContracts: Flow<List<TelecomContractEntity>> = appDao.getAllTelecomContracts()
  val housing: Flow<HousingEntity?> = appDao.getHousing()
  val allRentals: Flow<List<RentalContractEntity>> = appDao.getAllRentals()
  val monthlyBudget: Flow<MonthlyBudgetEntity?> = appDao.getMonthlyBudget()

  // Live Gold Rates State
  private val _goldRates = MutableStateFlow(
    GoldRates(
      price24kPerGram = 332.50,
      price22kPerGram = 304.79,
      price21kPerGram = 290.94,
      price18kPerGram = 249.38
    )
  )
  val goldRates: StateFlow<GoldRates> = _goldRates.asStateFlow()

  suspend fun refreshGoldPrices() {
    val rates = goldPriceService.fetchLiveGoldRates()
    _goldRates.value = rates
  }

  // Expenses CRUD
  suspend fun addExpense(expense: ExpenseEntity) = appDao.insertExpense(expense)
  suspend fun deleteExpense(expense: ExpenseEntity) = appDao.deleteExpense(expense)

  // Installments CRUD
  suspend fun addInstallment(
    title: String,
    provider: String,
    totalAmount: Double,
    monthsCount: Int,
    startDate: Long,
    notes: String = "",
    paidMonthsCount: Int = 0
  ) {
    // Smart validation per user requirements:
    // Tabby & Tamara: 2 to 24 months
    // SNB FLEX: strictly 4 months fixed
    val validatedMonths = when (provider) {
      "SNB_FLEX" -> 4
      "TABBY", "TAMARA" -> monthsCount.coerceIn(2, 24)
      else -> monthsCount.coerceAtLeast(1)
    }

    val monthly = Math.round((totalAmount / validatedMonths) * 100.0) / 100.0
    val safePaid = paidMonthsCount.coerceAtMost(validatedMonths)
    val newStatus = if (safePaid >= validatedMonths) "COMPLETED" else "ACTIVE"
    val plan = InstallmentPlanEntity(
      title = title,
      provider = provider,
      totalAmount = totalAmount,
      monthsCount = validatedMonths,
      monthlyAmount = monthly,
      startDate = startDate,
      paidMonthsCount = safePaid,
      status = newStatus,
      notes = notes
    )
    appDao.insertInstallment(plan)
  }

  suspend fun incrementPaidInstallment(installment: InstallmentPlanEntity) {
    val nextPaid = installment.paidMonthsCount + 1
    val newStatus = if (nextPaid >= installment.monthsCount) "COMPLETED" else "ACTIVE"
    val updated = installment.copy(
      paidMonthsCount = nextPaid.coerceAtMost(installment.monthsCount),
      status = newStatus
    )
    appDao.updateInstallment(updated)
  }

  suspend fun deleteInstallment(installment: InstallmentPlanEntity) {
    appDao.deleteInstallment(installment)
  }

  suspend fun updateInstallment(
    id: Long,
    title: String,
    provider: String,
    totalAmount: Double,
    monthsCount: Int,
    startDate: Long,
    paidMonthsCount: Int,
    notes: String = ""
  ) {
    val validatedMonths = when (provider) {
      "SNB_FLEX" -> 4
      "TABBY", "TAMARA" -> monthsCount.coerceIn(2, 24)
      else -> monthsCount.coerceAtLeast(1)
    }
    val monthly = Math.round((totalAmount / validatedMonths) * 100.0) / 100.0
    val newStatus = if (paidMonthsCount >= validatedMonths) "COMPLETED" else "ACTIVE"
    val updated = InstallmentPlanEntity(
      id = id,
      title = title,
      provider = provider,
      totalAmount = totalAmount,
      monthsCount = validatedMonths,
      monthlyAmount = monthly,
      startDate = startDate,
      paidMonthsCount = paidMonthsCount.coerceAtMost(validatedMonths),
      status = newStatus,
      notes = notes
    )
    appDao.updateInstallment(updated)
  }

  val backupManager = com.example.data.local.BackupManager(appDao)

  // Car Loan & Rescheduled Balloon Payment
  suspend fun updateCarLoan(
    carName: String,
    totalPrice: Double,
    downPayment: Double,
    termMonths: Int = 60,
    annualProfitRate: Double,
    balloonPayment: Double,
    startDate: Long,
    paidMonths: Int,
    balloonRefinanced: Boolean,
    balloonBankName: String,
    balloonTermMonths: Int
  ) {
    // Calculation:
    // Financed Amount = total - downPayment
    // Profit = (Financed - balloon) * (rate/100) * (termMonths / 12)
    val principalToAmortize = (totalPrice - downPayment - balloonPayment).coerceAtLeast(0.0)
    val totalProfit = principalToAmortize * (annualProfitRate / 100.0) * (termMonths / 12.0)
    val totalToPayInInstallments = principalToAmortize + totalProfit
    val calculatedMonthly = if (termMonths > 0) Math.round((totalToPayInInstallments / termMonths) * 100.0) / 100.0 else 0.0

    // Refinanced balloon calculation if user cannot pay in lump sum:
    val balloonProfit = if (balloonRefinanced) balloonPayment * (annualProfitRate / 100.0) * (balloonTermMonths / 12.0) else 0.0
    val balloonMonthly = if (balloonRefinanced && balloonTermMonths > 0) {
      Math.round(((balloonPayment + balloonProfit) / balloonTermMonths) * 100.0) / 100.0
    } else 0.0

    val entity = CarLoanEntity(
      id = 1,
      carName = carName,
      totalCarPrice = totalPrice,
      downPayment = downPayment,
      termMonths = termMonths,
      annualProfitRate = annualProfitRate,
      balloonPayment = balloonPayment,
      monthlyInstallment = calculatedMonthly,
      startDate = startDate,
      paidMonths = paidMonths,
      balloonRefinanced = balloonRefinanced,
      balloonBankName = balloonBankName,
      balloonTermMonths = balloonTermMonths,
      balloonMonthlyAmount = balloonMonthly
    )
    appDao.saveCarLoan(entity)
  }

  // Car Services
  suspend fun addCarService(service: CarServiceEntity) = appDao.insertCarService(service)
  suspend fun deleteCarService(service: CarServiceEntity) = appDao.deleteCarService(service)

  // Gold Holdings
  suspend fun addGoldHolding(
    itemType: String,
    karat: Int,
    weightGrams: Double,
    purchasePricePerGram: Double,
    purchaseDate: Long,
    notes: String = ""
  ) {
    val holding = GoldHoldingEntity(
      itemType = itemType,
      karat = karat,
      weightGrams = weightGrams,
      purchasePricePerGram = purchasePricePerGram,
      purchaseDate = purchaseDate,
      notes = notes
    )
    appDao.insertGoldHolding(holding)
  }

  suspend fun deleteGoldHolding(holding: GoldHoldingEntity) = appDao.deleteGoldHolding(holding)

  // Savings Goals
  suspend fun addSavingsGoal(goal: SavingsGoalEntity) = appDao.insertSavingsGoal(goal)
  suspend fun depositToSavingsGoal(goal: SavingsGoalEntity, depositAmount: Double) {
    val updated = goal.copy(currentAmount = goal.currentAmount + depositAmount)
    appDao.updateSavingsGoal(updated)
  }
  suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) = appDao.deleteSavingsGoal(goal)

  // Telecom Contracts & Device commitments
  suspend fun addTelecomContract(
    providerName: String,
    planMonthlyFee: Double,
    hasDeviceCommitment: Boolean,
    deviceName: String,
    deviceTotalCost: Double,
    deviceCommitmentMonths: Int,
    contractCount: Int = 1,
    startDate: Long = System.currentTimeMillis(),
    notes: String = ""
  ) {
    val deviceMonthly = if (hasDeviceCommitment && deviceCommitmentMonths > 0) {
      Math.round((deviceTotalCost / deviceCommitmentMonths) * 100.0) / 100.0
    } else 0.0

    val contract = TelecomContractEntity(
      providerName = providerName,
      planMonthlyFee = planMonthlyFee,
      hasDeviceCommitment = hasDeviceCommitment,
      deviceName = deviceName,
      deviceTotalCost = deviceTotalCost,
      deviceCommitmentMonths = deviceCommitmentMonths,
      deviceMonthlyCost = deviceMonthly,
      startDate = startDate,
      paidMonths = 0,
      contractCount = contractCount.coerceAtLeast(1),
      notes = notes
    )
    appDao.insertTelecomContract(contract)
  }

  suspend fun deleteTelecomContract(contract: TelecomContractEntity) = appDao.deleteTelecomContract(contract)

  // Housing & Utilities
  suspend fun saveHousing(
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
    val entity = HousingEntity(
      id = 1,
      propertyName = propertyName,
      rentAmount = rentAmount,
      paymentFrequency = paymentFrequency,
      waterAmount = waterAmount,
      isWaterIncluded = isWaterIncluded,
      electricityAmount = electricityAmount,
      isElectricityIncluded = isElectricityIncluded,
      nextDueDate = nextDueDate,
      notes = notes
    )
    appDao.saveHousing(entity)
  }

  // Incomes CRUD
  suspend fun addIncome(
    title: String,
    amount: Double,
    category: String,
    receiptDate: Long = System.currentTimeMillis(),
    isRecurringMonthly: Boolean = true,
    notes: String = ""
  ) {
    val entity = com.example.data.local.entity.IncomeEntity(
      title = title,
      amount = amount,
      category = category,
      receiptDate = receiptDate,
      isRecurringMonthly = isRecurringMonthly,
      notes = notes
    )
    appDao.insertIncome(entity)
  }

  suspend fun deleteIncome(income: com.example.data.local.entity.IncomeEntity) {
    appDao.deleteIncome(income)
  }

  // Custom Alerts CRUD
  suspend fun addAlert(
    title: String,
    alertType: String,
    targetAmount: Double,
    dueDate: Long,
    thresholdPercentage: Int,
    reason: String
  ) {
    val entity = com.example.data.local.entity.CustomAlertEntity(
      title = title,
      alertType = alertType,
      targetAmount = targetAmount,
      dueDate = dueDate,
      thresholdPercentage = thresholdPercentage,
      reason = reason,
      isEnabled = true,
      isTriggered = false
    )
    appDao.insertAlert(entity)
  }

  suspend fun toggleAlert(alert: com.example.data.local.entity.CustomAlertEntity, isEnabled: Boolean) {
    appDao.updateAlert(alert.copy(isEnabled = isEnabled))
  }

  suspend fun deleteAlert(alert: com.example.data.local.entity.CustomAlertEntity) {
    appDao.deleteAlert(alert)
  }

  suspend fun clearAllData() {
    appDao.clearAllData()
  }

  // Cloud Synchronization Methods
  suspend fun syncAllToCloud(userId: String): Result<Unit> {
    val fm = firestoreManager ?: return Result.failure(IllegalStateException("FirestoreManager is not configured"))
    return try {
      val expenses = allExpenses.first()
      val installments = allInstallments.first()
      val savings = allSavingsGoals.first()
      val incomes = allIncomes.first()
      val rentals = allRentals.first()
      fm.uploadAllToCloud(userId, expenses, installments, savings, incomes)
      rentals.forEach { fm.saveRental(userId, it) }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun syncAllFromCloud(userId: String): Result<Int> {
    val fm = firestoreManager ?: return Result.failure(IllegalStateException("FirestoreManager is not configured"))
    return try {
      val result = fm.fetchAllFromCloud(userId)
      if (result.isFailure) {
        return Result.failure(result.exceptionOrNull() ?: Exception("Cloud fetch failed"))
      }
      val payload = result.getOrNull() ?: return Result.success(0)
      var count = 0
      payload.expenses.forEach { appDao.insertExpense(it); count++ }
      payload.installments.forEach { appDao.insertInstallment(it); count++ }
      payload.savingsGoals.forEach { appDao.insertSavingsGoal(it); count++ }
      payload.incomes.forEach { appDao.insertIncome(it); count++ }
      payload.rentals.forEach { appDao.insertRental(it); count++ }
      Result.success(count)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun syncExpenseToCloud(userId: String?, expense: ExpenseEntity) {
    if (userId.isNullOrBlank() || firestoreManager == null) return
    firestoreManager.saveExpense(userId, expense)
  }

  suspend fun deleteExpenseFromCloud(userId: String?, expenseId: Long) {
    if (userId.isNullOrBlank() || firestoreManager == null) return
    firestoreManager.deleteExpense(userId, expenseId)
  }

  suspend fun syncInstallmentToCloud(userId: String?, plan: InstallmentPlanEntity) {
    if (userId.isNullOrBlank() || firestoreManager == null) return
    firestoreManager.saveInstallment(userId, plan)
  }

  suspend fun deleteInstallmentFromCloud(userId: String?, planId: Long) {
    if (userId.isNullOrBlank() || firestoreManager == null) return
    firestoreManager.deleteInstallment(userId, planId)
  }

  suspend fun syncSavingsGoalToCloud(userId: String?, goal: SavingsGoalEntity) {
    if (userId.isNullOrBlank() || firestoreManager == null) return
    firestoreManager.saveSavingsGoal(userId, goal)
  }

  suspend fun deleteSavingsGoalFromCloud(userId: String?, goalId: Long) {
    if (userId.isNullOrBlank() || firestoreManager == null) return
    firestoreManager.deleteSavingsGoal(userId, goalId)
  }

  suspend fun syncIncomeToCloud(userId: String?, income: IncomeEntity) {
    if (userId.isNullOrBlank() || firestoreManager == null) return
    firestoreManager.saveIncome(userId, income)
  }

  suspend fun deleteIncomeFromCloud(userId: String?, incomeId: Long) {
    if (userId.isNullOrBlank() || firestoreManager == null) return
    firestoreManager.deleteIncome(userId, incomeId)
  }

  // Car Plan Deletion / Reset
  suspend fun deleteCarLoan(userId: String? = null) {
    appDao.deleteCarLoan()
    if (!userId.isNullOrBlank() && firestoreManager != null) {
      firestoreManager.deleteCarLoan(userId)
    }
  }

  // Rental Contracts CRUD
  suspend fun addRental(rental: RentalContractEntity, userId: String? = null) {
    val id = appDao.insertRental(rental)
    if (!userId.isNullOrBlank() && firestoreManager != null) {
      firestoreManager.saveRental(userId, rental.copy(id = id))
    }
  }

  suspend fun updateRental(rental: RentalContractEntity, userId: String? = null) {
    appDao.updateRental(rental)
    if (!userId.isNullOrBlank() && firestoreManager != null) {
      firestoreManager.saveRental(userId, rental)
    }
  }

  suspend fun deleteRental(rental: RentalContractEntity, userId: String? = null) {
    appDao.deleteRental(rental)
    if (!userId.isNullOrBlank() && firestoreManager != null) {
      firestoreManager.deleteRental(userId, rental.id)
    }
  }

  // Monthly Budget Goal
  suspend fun saveMonthlyBudget(budget: MonthlyBudgetEntity) {
    appDao.saveMonthlyBudget(budget)
  }

  suspend fun resetAndRebuildDatabase(clearCloud: Boolean = false, userId: String? = null): Result<Unit> = runCatching {
    appDao.clearAllData()
    if (clearCloud && !userId.isNullOrBlank() && firestoreManager != null) {
      firestoreManager.clearAllUserData(userId)
    }
  }

  suspend fun fetchFirestoreMonthlyBreakdown(userId: String, isArabic: Boolean): Result<List<MonthBreakdownItem>> = runCatching {
    val fm = firestoreManager ?: return@runCatching emptyList()
    val expensesRes = fm.fetchExpenses(userId).getOrDefault(emptyList())
    val installmentsRes = fm.fetchInstallments(userId).getOrDefault(emptyList())
    val savingsRes = fm.fetchSavingsGoals(userId).getOrDefault(emptyList())

    generateMonthlyBreakdown(
      expenses = expensesRes,
      installments = installmentsRes,
      savingsGoals = savingsRes,
      isArabic = isArabic
    )
  }

  fun generateMonthlyBreakdown(
    expenses: List<ExpenseEntity>,
    installments: List<InstallmentPlanEntity>,
    savingsGoals: List<SavingsGoalEntity>,
    isArabic: Boolean
  ): List<MonthBreakdownItem> {
    val locale = if (isArabic) Locale("ar") else Locale.ENGLISH
    val monthFormat = SimpleDateFormat("MMM", locale)
    val keyFormat = SimpleDateFormat("yyyy-MM", Locale.ENGLISH)

    val result = mutableListOf<MonthBreakdownItem>()

    // Generate last 6 months (5 months ago up to current month)
    val monthKeys = mutableListOf<Triple<String, String, LongRange>>()
    for (i in 5 downTo 0) {
      val c = Calendar.getInstance()
      c.add(Calendar.MONTH, -i)
      c.set(Calendar.DAY_OF_MONTH, 1)
      c.set(Calendar.HOUR_OF_DAY, 0)
      c.set(Calendar.MINUTE, 0)
      c.set(Calendar.SECOND, 0)
      c.set(Calendar.MILLISECOND, 0)
      val startMs = c.timeInMillis

      c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
      c.set(Calendar.HOUR_OF_DAY, 23)
      c.set(Calendar.MINUTE, 59)
      c.set(Calendar.SECOND, 59)
      val endMs = c.timeInMillis

      val label = monthFormat.format(c.time)
      val key = keyFormat.format(c.time)
      monthKeys.add(Triple(key, label, startMs..endMs))
    }

    val totalSavingsAccumulated = savingsGoals.sumOf { it.currentAmount }
    val avgMonthlySavings = if (savingsGoals.isNotEmpty()) totalSavingsAccumulated / 6.0 else 0.0

    for ((key, label, range) in monthKeys) {
      val monthExpenses = expenses.filter { it.date in range }.sumOf { it.amount }
      val monthInstallments = installments.filter { plan ->
        val planStart = plan.startDate
        if (planStart <= range.last) {
          val planCal = Calendar.getInstance().apply { timeInMillis = planStart }
          val targetCal = Calendar.getInstance().apply { timeInMillis = range.first }
          val diffMonths = (targetCal.get(Calendar.YEAR) - planCal.get(Calendar.YEAR)) * 12 +
            (targetCal.get(Calendar.MONTH) - planCal.get(Calendar.MONTH))
          diffMonths in 0 until plan.monthsCount
        } else false
      }.sumOf { it.monthlyAmount }

      val monthSavings = if (avgMonthlySavings > 0) avgMonthlySavings else 0.0

      result.add(
        MonthBreakdownItem(
          monthKey = key,
          monthLabel = label,
          expenses = Math.round(monthExpenses * 100.0) / 100.0,
          installments = Math.round(monthInstallments * 100.0) / 100.0,
          savings = Math.round(monthSavings * 100.0) / 100.0
        )
      )
    }

    return result
  }
}

