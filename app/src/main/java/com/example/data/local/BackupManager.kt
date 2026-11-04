package com.example.data.local

import com.example.data.local.dao.AppDao
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
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

data class BackupStats(
  val expensesCount: Int,
  val incomesCount: Int,
  val installmentsCount: Int,
  val carServicesCount: Int,
  val goldHoldingsCount: Int,
  val savingsGoalsCount: Int,
  val telecomContractsCount: Int,
  val alertsCount: Int,
  val exportDate: Long
)

class BackupManager(private val appDao: AppDao) {

  suspend fun exportToJson(): String {
    val expenses = appDao.getAllExpenses().first()
    val incomes = appDao.getAllIncomes().first()
    val alerts = appDao.getAllAlerts().first()
    val installments = appDao.getAllInstallments().first()
    val carLoan = appDao.getCarLoan().first()
    val carServices = appDao.getAllCarServices().first()
    val gold = appDao.getAllGoldHoldings().first()
    val savings = appDao.getAllSavingsGoals().first()
    val telecom = appDao.getAllTelecomContracts().first()
    val housing = appDao.getHousing().first()

    val root = JSONObject()
    root.put("app", "Masareef")
    root.put("version", 2)
    root.put("timestamp", System.currentTimeMillis())

    // Expenses
    val expArr = JSONArray()
    expenses.forEach { e ->
      val obj = JSONObject()
      obj.put("title", e.title)
      obj.put("amount", e.amount)
      obj.put("category", e.category)
      obj.put("date", e.date)
      obj.put("isMonthlyRecurring", e.isMonthlyRecurring)
      obj.put("notes", e.notes)
      expArr.put(obj)
    }
    root.put("expenses", expArr)

    // Incomes
    val incArr = JSONArray()
    incomes.forEach { i ->
      val obj = JSONObject()
      obj.put("title", i.title)
      obj.put("amount", i.amount)
      obj.put("category", i.category)
      obj.put("receiptDate", i.receiptDate)
      obj.put("isRecurringMonthly", i.isRecurringMonthly)
      obj.put("notes", i.notes)
      incArr.put(obj)
    }
    root.put("incomes", incArr)

    // Installments
    val instArr = JSONArray()
    installments.forEach { inst ->
      val obj = JSONObject()
      obj.put("title", inst.title)
      obj.put("provider", inst.provider)
      obj.put("totalAmount", inst.totalAmount)
      obj.put("monthsCount", inst.monthsCount)
      obj.put("monthlyAmount", inst.monthlyAmount)
      obj.put("startDate", inst.startDate)
      obj.put("paidMonthsCount", inst.paidMonthsCount)
      obj.put("status", inst.status)
      obj.put("notes", inst.notes)
      instArr.put(obj)
    }
    root.put("installments", instArr)

    // Car Loan
    if (carLoan != null) {
      val carObj = JSONObject()
      carObj.put("carName", carLoan.carName)
      carObj.put("totalCarPrice", carLoan.totalCarPrice)
      carObj.put("downPayment", carLoan.downPayment)
      carObj.put("termMonths", carLoan.termMonths)
      carObj.put("annualProfitRate", carLoan.annualProfitRate)
      carObj.put("balloonPayment", carLoan.balloonPayment)
      carObj.put("monthlyInstallment", carLoan.monthlyInstallment)
      carObj.put("startDate", carLoan.startDate)
      carObj.put("paidMonths", carLoan.paidMonths)
      carObj.put("balloonRefinanced", carLoan.balloonRefinanced)
      carObj.put("balloonBankName", carLoan.balloonBankName)
      carObj.put("balloonTermMonths", carLoan.balloonTermMonths)
      carObj.put("balloonMonthlyAmount", carLoan.balloonMonthlyAmount)
      root.put("carLoan", carObj)
    }

    // Car Services
    val csArr = JSONArray()
    carServices.forEach { cs ->
      val obj = JSONObject()
      obj.put("serviceType", cs.serviceType)
      obj.put("cost", cs.cost)
      obj.put("date", cs.date)
      obj.put("currentOdometerKm", cs.currentOdometerKm)
      obj.put("nextDueOdometerKm", cs.nextDueOdometerKm)
      obj.put("nextDueDate", cs.nextDueDate)
      obj.put("notes", cs.notes)
      csArr.put(obj)
    }
    root.put("carServices", csArr)

    // Gold Holdings
    val goldArr = JSONArray()
    gold.forEach { g ->
      val obj = JSONObject()
      obj.put("itemType", g.itemType)
      obj.put("karat", g.karat)
      obj.put("weightGrams", g.weightGrams)
      obj.put("purchasePricePerGram", g.purchasePricePerGram)
      obj.put("purchaseDate", g.purchaseDate)
      obj.put("notes", g.notes)
      goldArr.put(obj)
    }
    root.put("goldHoldings", goldArr)

    // Savings Goals
    val savArr = JSONArray()
    savings.forEach { s ->
      val obj = JSONObject()
      obj.put("title", s.title)
      obj.put("targetAmount", s.targetAmount)
      obj.put("currentAmount", s.currentAmount)
      obj.put("deadlineDate", s.deadlineDate)
      obj.put("category", s.category)
      obj.put("notes", s.notes)
      savArr.put(obj)
    }
    root.put("savingsGoals", savArr)

    // Telecom
    val telArr = JSONArray()
    telecom.forEach { t ->
      val obj = JSONObject()
      obj.put("providerName", t.providerName)
      obj.put("planMonthlyFee", t.planMonthlyFee)
      obj.put("hasDeviceCommitment", t.hasDeviceCommitment)
      obj.put("deviceName", t.deviceName)
      obj.put("deviceTotalCost", t.deviceTotalCost)
      obj.put("deviceCommitmentMonths", t.deviceCommitmentMonths)
      obj.put("deviceMonthlyCost", t.deviceMonthlyCost)
      obj.put("startDate", t.startDate)
      obj.put("paidMonths", t.paidMonths)
      obj.put("contractCount", t.contractCount)
      obj.put("notes", t.notes)
      telArr.put(obj)
    }
    root.put("telecomContracts", telArr)

    // Housing
    if (housing != null) {
      val hObj = JSONObject()
      hObj.put("propertyName", housing.propertyName)
      hObj.put("rentAmount", housing.rentAmount)
      hObj.put("paymentFrequency", housing.paymentFrequency)
      hObj.put("waterAmount", housing.waterAmount)
      hObj.put("isWaterIncluded", housing.isWaterIncluded)
      hObj.put("electricityAmount", housing.electricityAmount)
      hObj.put("isElectricityIncluded", housing.isElectricityIncluded)
      hObj.put("nextDueDate", housing.nextDueDate)
      hObj.put("notes", housing.notes)
      root.put("housing", hObj)
    }

    // Alerts
    val altArr = JSONArray()
    alerts.forEach { a ->
      val obj = JSONObject()
      obj.put("title", a.title)
      obj.put("alertType", a.alertType)
      obj.put("targetAmount", a.targetAmount)
      obj.put("dueDate", a.dueDate)
      obj.put("thresholdPercentage", a.thresholdPercentage)
      obj.put("reason", a.reason)
      obj.put("isEnabled", a.isEnabled)
      altArr.put(obj)
    }
    root.put("alerts", altArr)

    return root.toString(2)
  }

  suspend fun inspectBackup(jsonStr: String): BackupStats? {
    return try {
      val root = JSONObject(jsonStr)
      BackupStats(
        expensesCount = root.optJSONArray("expenses")?.length() ?: 0,
        incomesCount = root.optJSONArray("incomes")?.length() ?: 0,
        installmentsCount = root.optJSONArray("installments")?.length() ?: 0,
        carServicesCount = root.optJSONArray("carServices")?.length() ?: 0,
        goldHoldingsCount = root.optJSONArray("goldHoldings")?.length() ?: 0,
        savingsGoalsCount = root.optJSONArray("savingsGoals")?.length() ?: 0,
        telecomContractsCount = root.optJSONArray("telecomContracts")?.length() ?: 0,
        alertsCount = root.optJSONArray("alerts")?.length() ?: 0,
        exportDate = root.optLong("timestamp", System.currentTimeMillis())
      )
    } catch (_: Exception) {
      null
    }
  }

  suspend fun importFromJson(jsonStr: String): Boolean {
    return try {
      val root = JSONObject(jsonStr)

      // Incomes
      root.optJSONArray("incomes")?.let { arr ->
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          appDao.insertIncome(
            IncomeEntity(
              title = obj.getString("title"),
              amount = obj.getDouble("amount"),
              category = obj.optString("category", "OTHER"),
              receiptDate = obj.optLong("receiptDate", System.currentTimeMillis()),
              isRecurringMonthly = obj.optBoolean("isRecurringMonthly", true),
              notes = obj.optString("notes", "")
            )
          )
        }
      }

      // Expenses
      root.optJSONArray("expenses")?.let { arr ->
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          appDao.insertExpense(
            ExpenseEntity(
              title = obj.getString("title"),
              amount = obj.getDouble("amount"),
              category = obj.optString("category", "FOOD"),
              date = obj.optLong("date", System.currentTimeMillis()),
              isMonthlyRecurring = obj.optBoolean("isMonthlyRecurring", false),
              notes = obj.optString("notes", "")
            )
          )
        }
      }

      // Installments
      root.optJSONArray("installments")?.let { arr ->
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          appDao.insertInstallment(
            InstallmentPlanEntity(
              title = obj.getString("title"),
              provider = obj.optString("provider", "TABBY"),
              totalAmount = obj.getDouble("totalAmount"),
              monthsCount = obj.getInt("monthsCount"),
              monthlyAmount = obj.optDouble("monthlyAmount", obj.getDouble("totalAmount") / obj.getInt("monthsCount")),
              startDate = obj.optLong("startDate", System.currentTimeMillis()),
              paidMonthsCount = obj.optInt("paidMonthsCount", 0),
              status = obj.optString("status", "ACTIVE"),
              notes = obj.optString("notes", "")
            )
          )
        }
      }

      // Car Loan
      root.optJSONObject("carLoan")?.let { carObj ->
        appDao.saveCarLoan(
          CarLoanEntity(
            id = 1,
            carName = carObj.optString("carName", "Car"),
            totalCarPrice = carObj.optDouble("totalCarPrice", 120000.0),
            downPayment = carObj.optDouble("downPayment", 15000.0),
            termMonths = carObj.optInt("termMonths", 60),
            annualProfitRate = carObj.optDouble("annualProfitRate", 3.5),
            balloonPayment = carObj.optDouble("balloonPayment", 35000.0),
            monthlyInstallment = carObj.optDouble("monthlyInstallment", 1680.0),
            startDate = carObj.optLong("startDate", System.currentTimeMillis()),
            paidMonths = carObj.optInt("paidMonths", 0),
            balloonRefinanced = carObj.optBoolean("balloonRefinanced", false),
            balloonBankName = carObj.optString("balloonBankName", "Al Rajhi Bank"),
            balloonTermMonths = carObj.optInt("balloonTermMonths", 24),
            balloonMonthlyAmount = carObj.optDouble("balloonMonthlyAmount", 1500.0)
          )
        )
      }

      // Car Services
      root.optJSONArray("carServices")?.let { arr ->
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          appDao.insertCarService(
            CarServiceEntity(
              serviceType = obj.getString("serviceType"),
              cost = obj.getDouble("cost"),
              date = obj.optLong("date", System.currentTimeMillis()),
              currentOdometerKm = obj.optInt("currentOdometerKm", 20000),
              nextDueOdometerKm = obj.optInt("nextDueOdometerKm", 30000),
              nextDueDate = obj.optLong("nextDueDate", System.currentTimeMillis()),
              notes = obj.optString("notes", "")
            )
          )
        }
      }

      // Gold Holdings
      root.optJSONArray("goldHoldings")?.let { arr ->
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          appDao.insertGoldHolding(
            GoldHoldingEntity(
              itemType = obj.optString("itemType", "BULLION_24K"),
              karat = obj.optInt("karat", 24),
              weightGrams = obj.getDouble("weightGrams"),
              purchasePricePerGram = obj.getDouble("purchasePricePerGram"),
              purchaseDate = obj.optLong("purchaseDate", System.currentTimeMillis()),
              notes = obj.optString("notes", "")
            )
          )
        }
      }

      // Savings Goals
      root.optJSONArray("savingsGoals")?.let { arr ->
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          appDao.insertSavingsGoal(
            SavingsGoalEntity(
              title = obj.getString("title"),
              targetAmount = obj.getDouble("targetAmount"),
              currentAmount = obj.optDouble("currentAmount", 0.0),
              deadlineDate = obj.optLong("deadlineDate", System.currentTimeMillis()),
              category = obj.optString("category", "EMERGENCY_FUND"),
              notes = obj.optString("notes", "")
            )
          )
        }
      }

      // Telecom
      root.optJSONArray("telecomContracts")?.let { arr ->
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          appDao.insertTelecomContract(
            TelecomContractEntity(
              providerName = obj.getString("providerName"),
              planMonthlyFee = obj.getDouble("planMonthlyFee"),
              hasDeviceCommitment = obj.optBoolean("hasDeviceCommitment", false),
              deviceName = obj.optString("deviceName", ""),
              deviceTotalCost = obj.optDouble("deviceTotalCost", 0.0),
              deviceCommitmentMonths = obj.optInt("deviceCommitmentMonths", 24),
              deviceMonthlyCost = obj.optDouble("deviceMonthlyCost", 0.0),
              startDate = obj.optLong("startDate", System.currentTimeMillis()),
              paidMonths = obj.optInt("paidMonths", 0),
              contractCount = obj.optInt("contractCount", 1),
              notes = obj.optString("notes", "")
            )
          )
        }
      }

      // Housing
      root.optJSONObject("housing")?.let { hObj ->
        appDao.saveHousing(
          HousingEntity(
            id = 1,
            propertyName = hObj.optString("propertyName", "Home"),
            rentAmount = hObj.optDouble("rentAmount", 3000.0),
            paymentFrequency = hObj.optString("paymentFrequency", "MONTHLY"),
            waterAmount = hObj.optDouble("waterAmount", 120.0),
            isWaterIncluded = hObj.optBoolean("isWaterIncluded", true),
            electricityAmount = hObj.optDouble("electricityAmount", 350.0),
            isElectricityIncluded = hObj.optBoolean("isElectricityIncluded", true),
            nextDueDate = hObj.optLong("nextDueDate", System.currentTimeMillis()),
            notes = hObj.optString("notes", "")
          )
        )
      }

      // Alerts
      root.optJSONArray("alerts")?.let { arr ->
        for (i in 0 until arr.length()) {
          val obj = arr.getJSONObject(i)
          appDao.insertAlert(
            CustomAlertEntity(
              title = obj.getString("title"),
              alertType = obj.optString("alertType", "GENERAL"),
              targetAmount = obj.optDouble("targetAmount", 0.0),
              dueDate = obj.optLong("dueDate", System.currentTimeMillis()),
              thresholdPercentage = obj.optInt("thresholdPercentage", 80),
              reason = obj.optString("reason", ""),
              isEnabled = obj.optBoolean("isEnabled", true)
            )
          )
        }
      }

      true
    } catch (_: Exception) {
      false
    }
  }
}
