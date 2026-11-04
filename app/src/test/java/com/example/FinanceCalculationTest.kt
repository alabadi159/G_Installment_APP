package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinanceCalculationTest {

  @Test
  fun testSnbFlexFixedFourMonths() {
    val totalAmount = 6000.0
    // SNB FLEX is strictly 4 months fixed
    val months = 4
    val monthly = Math.round((totalAmount / months) * 100.0) / 100.0
    assertEquals(1500.0, monthly, 0.01)
  }

  @Test
  fun testTabbyTamaraVariableMonths() {
    val totalAmount = 4800.0
    val months = 6 // 2 to 24 months selectable
    val monthly = Math.round((totalAmount / months) * 100.0) / 100.0
    assertEquals(800.0, monthly, 0.01)
  }

  @Test
  fun testCarLoanBalloonRescheduling() {
    val balloonPayment = 36000.0
    val bankTenureMonths = 24
    val rate = 3.5
    val profit = balloonPayment * (rate / 100.0) * (bankTenureMonths / 12.0)
    val monthlyBalloon = Math.round(((balloonPayment + profit) / bankTenureMonths) * 100.0) / 100.0
    assertTrue(monthlyBalloon > 1500.0)
  }

  @Test
  fun testGoldKaratProportions() {
    val price24k = 332.0
    val price21k = Math.round((price24k * (21.0 / 24.0)) * 100.0) / 100.0
    val price18k = Math.round((price24k * (18.0 / 24.0)) * 100.0) / 100.0

    assertEquals(290.5, price21k, 0.5)
    assertEquals(249.0, price18k, 0.5)
  }

  @Test
  fun testTelecomDeviceMonthlyInstallment() {
    val deviceCost = 5400.0
    val months = 24
    val monthlyDevice = Math.round((deviceCost / months) * 100.0) / 100.0
    assertEquals(225.0, monthlyDevice, 0.01)
  }

  @Test
  fun testNetCashflowAndSavingsRate() {
    val monthlyIncome = 20000.0
    val totalObligations = 8500.0
    val expenses = 4500.0
    val netCashflow = monthlyIncome - (totalObligations + expenses)
    val savingsRate = (netCashflow / monthlyIncome) * 100.0

    assertEquals(7000.0, netCashflow, 0.01)
    assertEquals(35.0, savingsRate, 0.01)
  }

  @Test
  fun testBudgetThresholdAlert() {
    val budgetLimit = 5000.0
    val currentSpent = 4200.0
    val thresholdPct = 80 // 80%
    val isTriggered = (currentSpent / budgetLimit * 100.0) >= thresholdPct
    assertTrue(isTriggered)
  }
}
