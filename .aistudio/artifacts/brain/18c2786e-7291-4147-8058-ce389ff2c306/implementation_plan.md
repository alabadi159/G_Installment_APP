# Implementation Plan: Spending Budget Goal, Multi-Rental Management, Deletable Car Plan & Zero-Data State

## Overview
Implement:
1. **Monthly Spending Budget Goal Tracker:** A customizable spending limit with an interactive progress bar showing warning status at 80% (Amber) and exceeding alert at 100% (Red).
2. **Car Plan Management & Reset:** Full delete, reset, and create new car plan capabilities so users have complete control and no non-deletable default values.
3. **Dedicated Multi-Rental Section:** Unified tracking for properties & assets (Houses, Commercial Stores, Rental Cars, Places/Warehouses) with category filters, payment cycles, and utility inclusion flags.
4. **Pure Empty State on Fresh Launch:** Ensure all tables and state start 100% empty, allowing users to enter all their own records from scratch.

---

## User Decisions
- **Budget Warning Thresholds:** Warning trigger at **80%** (amber) and over-budget alert at **100%** (red).
- **Rental Organization:** Dedicated **Multi-Rental section** with quick category filter chips (Houses, Stores, Cars, Places).
- **Car Plan Management:** Full delete, reset, and create new plan from scratch.
- **Initial Data State:** 100% clean and empty across all features.

---

## Proposed Changes

### 1. Data Layer (`com.example.data`)
- **`MonthlyBudgetEntity.kt` / Preferences:**
  - Store monthly spending budget target (e.g., in a dedicated single-row Room table or SharedPreferences/DataStore).
- **`RentalContractEntity.kt`:**
  - Create entity for rentals: `id`, `name`, `rentalType` (`HOUSE`, `STORE`, `CAR`, `PLACE`), `monthlyAmount`, `paymentFrequency` (`MONTHLY`, `QUARTERLY`, `ANNUAL`), `includesWaterElectricity`, `startDate`, `durationMonths`, `notes`.
  - Add corresponding Room DAO queries in `AppDao.kt`.
- **`AppDao.kt` & `AppDatabase.kt`:**
  - Add CRUD for `RentalContractEntity` and `MonthlyBudgetEntity`.
  - Update `deleteCarLoan()` to completely erase any active car loan row without leaving undeletable remnants.
  - Bump Room DB version to accommodate new entities and wipe out any legacy seed data.
- **`FirestoreManager.kt`:**
  - Add sync support for rentals and budget goals to Firebase.

### 2. ViewModel Layer (`com.example.ui.viewmodel`)
- **`FinanceViewModel.kt`:**
  - Add `monthlyBudgetGoal: StateFlow<Double>` and `updateBudgetGoal(newGoal: Double)`.
  - Calculate `budgetSpendingProgress: StateFlow<BudgetProgressState>` (current month spending, goal amount, percentage, status: `NORMAL`, `WARNING_80`, `EXCEEDED_100`).
  - Add `rentals: StateFlow<List<RentalContractEntity>>` and CRUD operations (`addRental`, `deleteRental`, `updateRental`).
  - Add `deleteCarLoan()` and `resetCarLoan()` methods that remove the car loan completely.
  - Enhance `resetAndRebuildDatabase` to completely clear car loan, rentals, budget, and all financial tables to a 0-item state.

### 3. UI Layer (`com.example.ui`)
- **`DashboardScreen.kt`:**
  - Add **Monthly Spending Budget Goal Card** featuring:
    - Current month spending vs. user-defined budget goal.
    - Animated progress bar changing dynamically from Green/Teal (<80%) to Amber (80%–99%) to Red (>=100%).
    - Warning/Alert chip with custom warning description when approaching or exceeding the budget.
    - Quick "Edit Budget Goal" dialog button.
- **`CarScreen.kt`:**
  - Add a "Delete / Reset Car Plan" button with confirmation dialog.
  - When no car loan exists, render an attractive empty state with an "Add Car Financing Plan" button.
- **`RentalsScreen.kt` (New Screen) & Navigation:**
  - Implement a dedicated screen with filter chips: **All**, **Houses**, **Stores**, **Rental Cars**, **Places/Offices**.
  - Show summary cards for total monthly rent and active contracts.
  - Add `AddRentalDialog` to input rental type, property name, rent amount, payment cycle, utility inclusion, and duration.
  - Wire into bottom navigation or dashboard navigation cards.
- **`Localization.kt`:**
  - Add bilingual (Arabic & English) strings for budget tracker, warning statuses, car plan deletion, and rental contract types.

---

## Verification Plan

### Automated Build Verification
- Execute `compile_applet` to ensure zero compilation or syntax errors across new entities, DAOs, screens, and dialogs.
- Mechanical verification for no forbidden patterns or credentials.

### Feature Flow Verification
- Verify setting a budget goal displays the progress bar accurately and changes color at >=80% and >=100%.
- Verify deleting the car loan results in a clean empty state with no undeletable data remaining.
- Verify adding and filtering rental records (houses, stores, cars, places) works smoothly.
- Verify running "Reset & Rebuild Database" leaves every single section completely empty.
