package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AppDao
import com.example.data.local.entity.CarLoanEntity
import com.example.data.local.entity.CarServiceEntity
import com.example.data.local.entity.CustomAlertEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.GoldHoldingEntity
import com.example.data.local.entity.HousingEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.InstallmentPlanEntity
import com.example.data.local.entity.MonthlyBudgetEntity
import com.example.data.local.entity.RentalContractEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TelecomContractEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    ExpenseEntity::class,
    IncomeEntity::class,
    CustomAlertEntity::class,
    InstallmentPlanEntity::class,
    CarLoanEntity::class,
    CarServiceEntity::class,
    GoldHoldingEntity::class,
    SavingsGoalEntity::class,
    TelecomContractEntity::class,
    HousingEntity::class,
    RentalContractEntity::class,
    MonthlyBudgetEntity::class
  ],
  version = 4,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun appDao(): AppDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "masareef_database"
        )
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}

