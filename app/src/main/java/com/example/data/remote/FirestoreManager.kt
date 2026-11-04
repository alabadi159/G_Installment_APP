package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.InstallmentPlanEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class CloudSyncPayload(
  val expenses: List<ExpenseEntity> = emptyList(),
  val installments: List<InstallmentPlanEntity> = emptyList(),
  val savingsGoals: List<SavingsGoalEntity> = emptyList(),
  val incomes: List<IncomeEntity> = emptyList()
)

class FirestoreManager(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  // ----------------------------------------------------
  // Expenses CRUD
  // ----------------------------------------------------

  suspend fun saveExpense(userId: String, expense: ExpenseEntity): Result<Unit> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val docId = if (expense.id > 0) expense.id.toString() else System.currentTimeMillis().toString()
    val docRef = db.collection("users").document(userId)
      .collection("expenses").document(docId)

    val data = hashMapOf<String, Any>(
      "title" to expense.title,
      "amount" to expense.amount,
      "category" to expense.category,
      "date" to expense.date,
      "isMonthlyRecurring" to expense.isMonthlyRecurring,
      "notes" to expense.notes,
      "updatedAt" to FieldValue.serverTimestamp()
    )

    try {
      docRef.set(data, SetOptions.merge()).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun deleteExpense(userId: String, expenseId: Long): Result<Unit> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val docRef = db.collection("users").document(userId)
      .collection("expenses").document(expenseId.toString())
    try {
      docRef.delete().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun fetchExpenses(userId: String): Result<List<ExpenseEntity>> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val colRef = db.collection("users").document(userId).collection("expenses")
    try {
      val snapshot = colRef.get().await()
      snapshot.documents.mapNotNull { doc ->
        val title = doc.getString("title") ?: return@mapNotNull null
        val amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0
        val category = doc.getString("category") ?: "OTHER"
        val date = (doc.get("date") as? Number)?.toLong() ?: System.currentTimeMillis()
        val isRecurring = doc.getBoolean("isMonthlyRecurring") ?: false
        val notes = doc.getString("notes") ?: ""
        val id = doc.id.toLongOrNull() ?: 0L

        ExpenseEntity(
          id = id,
          title = title,
          amount = amount,
          category = category,
          date = date,
          isMonthlyRecurring = isRecurring,
          notes = notes
        )
      }
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, colRef.path)
      throw e
    }
  }

  fun observeExpenses(userId: String): Flow<List<ExpenseEntity>> = callbackFlow {
    if (userId.isBlank()) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }
    val colRef = db.collection("users").document(userId).collection("expenses")
    val registration = colRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, colRef.path)
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val list = snapshot.documents.mapNotNull { doc ->
          val title = doc.getString("title") ?: return@mapNotNull null
          val amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0
          val category = doc.getString("category") ?: "OTHER"
          val date = (doc.get("date") as? Number)?.toLong() ?: System.currentTimeMillis()
          val isRecurring = doc.getBoolean("isMonthlyRecurring") ?: false
          val notes = doc.getString("notes") ?: ""
          val id = doc.id.toLongOrNull() ?: 0L

          ExpenseEntity(
            id = id,
            title = title,
            amount = amount,
            category = category,
            date = date,
            isMonthlyRecurring = isRecurring,
            notes = notes
          )
        }
        trySend(list)
      }
    }
    awaitClose { registration.remove() }
  }

  // ----------------------------------------------------
  // Installments CRUD
  // ----------------------------------------------------

  suspend fun saveInstallment(userId: String, plan: InstallmentPlanEntity): Result<Unit> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val docId = if (plan.id > 0) plan.id.toString() else System.currentTimeMillis().toString()
    val docRef = db.collection("users").document(userId)
      .collection("installments").document(docId)

    val data = hashMapOf<String, Any>(
      "title" to plan.title,
      "provider" to plan.provider,
      "totalAmount" to plan.totalAmount,
      "monthsCount" to plan.monthsCount,
      "monthlyAmount" to plan.monthlyAmount,
      "startDate" to plan.startDate,
      "paidMonthsCount" to plan.paidMonthsCount,
      "status" to plan.status,
      "notes" to plan.notes,
      "updatedAt" to FieldValue.serverTimestamp()
    )

    try {
      docRef.set(data, SetOptions.merge()).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun deleteInstallment(userId: String, planId: Long): Result<Unit> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val docRef = db.collection("users").document(userId)
      .collection("installments").document(planId.toString())
    try {
      docRef.delete().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun fetchInstallments(userId: String): Result<List<InstallmentPlanEntity>> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val colRef = db.collection("users").document(userId).collection("installments")
    try {
      val snapshot = colRef.get().await()
      snapshot.documents.mapNotNull { doc ->
        val title = doc.getString("title") ?: return@mapNotNull null
        val provider = doc.getString("provider") ?: "TABBY"
        val totalAmount = (doc.get("totalAmount") as? Number)?.toDouble() ?: 0.0
        val monthsCount = (doc.get("monthsCount") as? Number)?.toInt() ?: 4
        val monthlyAmount = (doc.get("monthlyAmount") as? Number)?.toDouble() ?: (totalAmount / monthsCount)
        val startDate = (doc.get("startDate") as? Number)?.toLong() ?: System.currentTimeMillis()
        val paidMonthsCount = (doc.get("paidMonthsCount") as? Number)?.toInt() ?: 0
        val status = doc.getString("status") ?: "ACTIVE"
        val notes = doc.getString("notes") ?: ""
        val id = doc.id.toLongOrNull() ?: 0L

        InstallmentPlanEntity(
          id = id,
          title = title,
          provider = provider,
          totalAmount = totalAmount,
          monthsCount = monthsCount,
          monthlyAmount = monthlyAmount,
          startDate = startDate,
          paidMonthsCount = paidMonthsCount,
          status = status,
          notes = notes
        )
      }
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, colRef.path)
      throw e
    }
  }

  fun observeInstallments(userId: String): Flow<List<InstallmentPlanEntity>> = callbackFlow {
    if (userId.isBlank()) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }
    val colRef = db.collection("users").document(userId).collection("installments")
    val registration = colRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, colRef.path)
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val list = snapshot.documents.mapNotNull { doc ->
          val title = doc.getString("title") ?: return@mapNotNull null
          val provider = doc.getString("provider") ?: "TABBY"
          val totalAmount = (doc.get("totalAmount") as? Number)?.toDouble() ?: 0.0
          val monthsCount = (doc.get("monthsCount") as? Number)?.toInt() ?: 4
          val monthlyAmount = (doc.get("monthlyAmount") as? Number)?.toDouble() ?: (totalAmount / monthsCount)
          val startDate = (doc.get("startDate") as? Number)?.toLong() ?: System.currentTimeMillis()
          val paidMonthsCount = (doc.get("paidMonthsCount") as? Number)?.toInt() ?: 0
          val status = doc.getString("status") ?: "ACTIVE"
          val notes = doc.getString("notes") ?: ""
          val id = doc.id.toLongOrNull() ?: 0L

          InstallmentPlanEntity(
            id = id,
            title = title,
            provider = provider,
            totalAmount = totalAmount,
            monthsCount = monthsCount,
            monthlyAmount = monthlyAmount,
            startDate = startDate,
            paidMonthsCount = paidMonthsCount,
            status = status,
            notes = notes
          )
        }
        trySend(list)
      }
    }
    awaitClose { registration.remove() }
  }

  // ----------------------------------------------------
  // Savings Goals CRUD
  // ----------------------------------------------------

  suspend fun saveSavingsGoal(userId: String, goal: SavingsGoalEntity): Result<Unit> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val docId = if (goal.id > 0) goal.id.toString() else System.currentTimeMillis().toString()
    val docRef = db.collection("users").document(userId)
      .collection("savings_goals").document(docId)

    val data = hashMapOf<String, Any>(
      "title" to goal.title,
      "targetAmount" to goal.targetAmount,
      "currentAmount" to goal.currentAmount,
      "deadlineDate" to goal.deadlineDate,
      "category" to goal.category,
      "notes" to goal.notes,
      "updatedAt" to FieldValue.serverTimestamp()
    )

    try {
      docRef.set(data, SetOptions.merge()).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun deleteSavingsGoal(userId: String, goalId: Long): Result<Unit> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val docRef = db.collection("users").document(userId)
      .collection("savings_goals").document(goalId.toString())
    try {
      docRef.delete().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun fetchSavingsGoals(userId: String): Result<List<SavingsGoalEntity>> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val colRef = db.collection("users").document(userId).collection("savings_goals")
    try {
      val snapshot = colRef.get().await()
      snapshot.documents.mapNotNull { doc ->
        val title = doc.getString("title") ?: return@mapNotNull null
        val targetAmount = (doc.get("targetAmount") as? Number)?.toDouble() ?: 0.0
        val currentAmount = (doc.get("currentAmount") as? Number)?.toDouble() ?: 0.0
        val deadlineDate = (doc.get("deadlineDate") as? Number)?.toLong() ?: System.currentTimeMillis()
        val category = doc.getString("category") ?: "EMERGENCY_FUND"
        val notes = doc.getString("notes") ?: ""
        val id = doc.id.toLongOrNull() ?: 0L

        SavingsGoalEntity(
          id = id,
          title = title,
          targetAmount = targetAmount,
          currentAmount = currentAmount,
          deadlineDate = deadlineDate,
          category = category,
          notes = notes
        )
      }
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, colRef.path)
      throw e
    }
  }

  fun observeSavingsGoals(userId: String): Flow<List<SavingsGoalEntity>> = callbackFlow {
    if (userId.isBlank()) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }
    val colRef = db.collection("users").document(userId).collection("savings_goals")
    val registration = colRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, colRef.path)
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val list = snapshot.documents.mapNotNull { doc ->
          val title = doc.getString("title") ?: return@mapNotNull null
          val targetAmount = (doc.get("targetAmount") as? Number)?.toDouble() ?: 0.0
          val currentAmount = (doc.get("currentAmount") as? Number)?.toDouble() ?: 0.0
          val deadlineDate = (doc.get("deadlineDate") as? Number)?.toLong() ?: System.currentTimeMillis()
          val category = doc.getString("category") ?: "EMERGENCY_FUND"
          val notes = doc.getString("notes") ?: ""
          val id = doc.id.toLongOrNull() ?: 0L

          SavingsGoalEntity(
            id = id,
            title = title,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            deadlineDate = deadlineDate,
            category = category,
            notes = notes
          )
        }
        trySend(list)
      }
    }
    awaitClose { registration.remove() }
  }

  // ----------------------------------------------------
  // Incomes CRUD
  // ----------------------------------------------------

  suspend fun saveIncome(userId: String, income: IncomeEntity): Result<Unit> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val docId = if (income.id > 0) income.id.toString() else System.currentTimeMillis().toString()
    val docRef = db.collection("users").document(userId)
      .collection("incomes").document(docId)

    val data = hashMapOf<String, Any>(
      "title" to income.title,
      "amount" to income.amount,
      "category" to income.category,
      "receiptDate" to income.receiptDate,
      "isRecurringMonthly" to income.isRecurringMonthly,
      "notes" to income.notes,
      "updatedAt" to FieldValue.serverTimestamp()
    )

    try {
      docRef.set(data, SetOptions.merge()).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun deleteIncome(userId: String, incomeId: Long): Result<Unit> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val docRef = db.collection("users").document(userId)
      .collection("incomes").document(incomeId.toString())
    try {
      docRef.delete().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun fetchIncomes(userId: String): Result<List<IncomeEntity>> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val colRef = db.collection("users").document(userId).collection("incomes")
    try {
      val snapshot = colRef.get().await()
      snapshot.documents.mapNotNull { doc ->
        val title = doc.getString("title") ?: return@mapNotNull null
        val amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0
        val category = doc.getString("category") ?: "SALARY"
        val receiptDate = (doc.get("receiptDate") as? Number)?.toLong() ?: System.currentTimeMillis()
        val isRecurring = doc.getBoolean("isRecurringMonthly") ?: true
        val notes = doc.getString("notes") ?: ""
        val id = doc.id.toLongOrNull() ?: 0L

        IncomeEntity(
          id = id,
          title = title,
          amount = amount,
          category = category,
          receiptDate = receiptDate,
          isRecurringMonthly = isRecurring,
          notes = notes
        )
      }
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, colRef.path)
      throw e
    }
  }

  // ----------------------------------------------------
  // Full Cloud Sync Operations
  // ----------------------------------------------------

  suspend fun uploadAllToCloud(
    userId: String,
    expenses: List<ExpenseEntity>,
    installments: List<InstallmentPlanEntity>,
    savings: List<SavingsGoalEntity>,
    incomes: List<IncomeEntity>
  ): Result<Unit> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }

    // Upload expenses
    expenses.forEach { expense ->
      saveExpense(userId, expense)
    }

    // Upload installments
    installments.forEach { plan ->
      saveInstallment(userId, plan)
    }

    // Upload savings goals
    savings.forEach { goal ->
      saveSavingsGoal(userId, goal)
    }

    // Upload incomes
    incomes.forEach { income ->
      saveIncome(userId, income)
    }
  }

  suspend fun fetchAllFromCloud(userId: String): Result<CloudSyncPayload> = runCatching {
    require(userId.isNotBlank()) { "User ID cannot be blank" }
    val expenses = fetchExpenses(userId).getOrDefault(emptyList())
    val installments = fetchInstallments(userId).getOrDefault(emptyList())
    val savings = fetchSavingsGoals(userId).getOrDefault(emptyList())
    val incomes = fetchIncomes(userId).getOrDefault(emptyList())

    CloudSyncPayload(
      expenses = expenses,
      installments = installments,
      savingsGoals = savings,
      incomes = incomes
    )
  }
}
