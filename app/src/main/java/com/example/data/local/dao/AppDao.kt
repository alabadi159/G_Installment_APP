package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
  // Expenses
  @Query("SELECT * FROM expenses ORDER BY date DESC")
  fun getAllExpenses(): Flow<List<ExpenseEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: ExpenseEntity): Long

  @Delete
  suspend fun deleteExpense(expense: ExpenseEntity)

  // Incomes
  @Query("SELECT * FROM incomes ORDER BY receiptDate DESC")
  fun getAllIncomes(): Flow<List<IncomeEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertIncome(income: IncomeEntity): Long

  @Delete
  suspend fun deleteIncome(income: IncomeEntity)

  // Custom Alerts & Notifications
  @Query("SELECT * FROM custom_alerts ORDER BY dueDate ASC")
  fun getAllAlerts(): Flow<List<CustomAlertEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlert(alert: CustomAlertEntity): Long

  @Update
  suspend fun updateAlert(alert: CustomAlertEntity)

  @Delete
  suspend fun deleteAlert(alert: CustomAlertEntity)

  // Installments
  @Query("SELECT * FROM installments ORDER BY id DESC")
  fun getAllInstallments(): Flow<List<InstallmentPlanEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInstallment(installment: InstallmentPlanEntity): Long

  @Update
  suspend fun updateInstallment(installment: InstallmentPlanEntity)

  @Delete
  suspend fun deleteInstallment(installment: InstallmentPlanEntity)

  // Car Loan
  @Query("SELECT * FROM car_loan WHERE id = 1 LIMIT 1")
  fun getCarLoan(): Flow<CarLoanEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveCarLoan(carLoan: CarLoanEntity)

  // Car Services
  @Query("SELECT * FROM car_services ORDER BY date DESC")
  fun getAllCarServices(): Flow<List<CarServiceEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCarService(service: CarServiceEntity): Long

  @Delete
  suspend fun deleteCarService(service: CarServiceEntity)

  // Gold Holdings
  @Query("SELECT * FROM gold_holdings ORDER BY purchaseDate DESC")
  fun getAllGoldHoldings(): Flow<List<GoldHoldingEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGoldHolding(holding: GoldHoldingEntity): Long

  @Delete
  suspend fun deleteGoldHolding(holding: GoldHoldingEntity)

  // Savings Goals
  @Query("SELECT * FROM savings_goals ORDER BY id ASC")
  fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long

  @Update
  suspend fun updateSavingsGoal(goal: SavingsGoalEntity)

  @Delete
  suspend fun deleteSavingsGoal(goal: SavingsGoalEntity)

  // Telecom Contracts
  @Query("SELECT * FROM telecom_contracts ORDER BY id DESC")
  fun getAllTelecomContracts(): Flow<List<TelecomContractEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTelecomContract(contract: TelecomContractEntity): Long

  @Update
  suspend fun updateTelecomContract(contract: TelecomContractEntity)

  @Delete
  suspend fun deleteTelecomContract(contract: TelecomContractEntity)

  // Housing
  @Query("SELECT * FROM housing_expenses WHERE id = 1 LIMIT 1")
  fun getHousing(): Flow<HousingEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveHousing(housing: HousingEntity)

  // Clear all data queries
  @Query("DELETE FROM expenses")
  suspend fun deleteAllExpenses()

  @Query("DELETE FROM incomes")
  suspend fun deleteAllIncomes()

  @Query("DELETE FROM custom_alerts")
  suspend fun deleteAllAlerts()

  @Query("DELETE FROM installments")
  suspend fun deleteAllInstallments()

  @Query("DELETE FROM car_loan")
  suspend fun deleteCarLoan()

  @Query("DELETE FROM car_services")
  suspend fun deleteAllCarServices()

  @Query("DELETE FROM gold_holdings")
  suspend fun deleteAllGoldHoldings()

  @Query("DELETE FROM savings_goals")
  suspend fun deleteAllSavingsGoals()

  @Query("DELETE FROM telecom_contracts")
  suspend fun deleteAllTelecomContracts()

  @Query("DELETE FROM housing_expenses")
  suspend fun deleteHousing()
}
