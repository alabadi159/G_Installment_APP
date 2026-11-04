package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

data class ChartSlice(
  val label: String,
  val value: Double,
  val color: Color
)

@Composable
fun DonutBreakdownChart(
  slices: List<ChartSlice>,
  totalAmountText: String,
  centerLabel: String,
  modifier: Modifier = Modifier,
  strokeWidth: Dp = 24.dp
) {
  val total = slices.sumOf { it.value }
  val validSlices = if (total > 0) slices else emptyList()

  val animatedProgress by animateFloatAsState(
    targetValue = 1f,
    animationSpec = tween(durationMillis = 800),
    label = "donutProgress"
  )

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(190.dp)
        .padding(8.dp),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.size(174.dp)) {
        val diameter = size.minDimension
        val stroke = strokeWidth.toPx()
        val arcSize = Size(diameter - stroke, diameter - stroke)
        val topLeft = Offset(stroke / 2, stroke / 2)

        if (validSlices.isEmpty()) {
          drawArc(
            color = Color.Gray.copy(alpha = 0.2f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke)
          )
        } else {
          var startAngle = -90f
          validSlices.forEach { slice ->
            val sweep = ((slice.value / total) * 360f * animatedProgress).toFloat()
            drawArc(
              color = slice.color,
              startAngle = startAngle,
              sweepAngle = sweep,
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            startAngle += sweep
          }
        }
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = totalAmountText,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = centerLabel,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Legend
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      slices.filter { it.value > 0 }.forEach { slice ->
        val pct = if (total > 0) (slice.value / total * 100.0) else 0.0
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              modifier = Modifier.size(10.dp),
              shape = CircleShape,
              color = slice.color
            ) {}
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = slice.label,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Text(
            text = "${String.format("%.1f", pct)}% (${String.format("%.0f", slice.value)})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun HorizontalProgressBar(
  progress: Float,
  color: Color,
  modifier: Modifier = Modifier,
  trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
  height: Dp = 8.dp
) {
  val clamped = progress.coerceIn(0f, 1f)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
  ) {
    Canvas(modifier = Modifier.matchParentSize()) {
      drawRoundRect(
        color = trackColor,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
      )
      if (clamped > 0f) {
        drawRoundRect(
          color = color,
          size = Size(size.width * clamped, size.height),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
        )
      }
    }
  }
}

@Composable
fun CarLoanBreakdownBar(
  downPayment: Double,
  financedPrincipal: Double,
  totalProfit: Double,
  balloonPayment: Double,
  language: AppLanguage,
  modifier: Modifier = Modifier
) {
  val total = downPayment + financedPrincipal + totalProfit + balloonPayment
  if (total <= 0) return

  val dpPct = (downPayment / total).toFloat()
  val fpPct = (financedPrincipal / total).toFloat()
  val prPct = (totalProfit / total).toFloat()
  val blPct = (balloonPayment / total).toFloat()

  Column(modifier = modifier.fillMaxWidth()) {
    Surface(
      modifier = Modifier.fillMaxWidth().height(16.dp),
      shape = RoundedCornerShape(8.dp),
      color = Color.Transparent
    ) {
      Row(modifier = Modifier.fillMaxWidth()) {
        if (dpPct > 0) {
          Surface(
            modifier = Modifier.weight(dpPct.coerceAtLeast(0.01f)).height(16.dp),
            color = Color(0xFF10B981)
          ) {}
        }
        if (fpPct > 0) {
          Surface(
            modifier = Modifier.weight(fpPct.coerceAtLeast(0.01f)).height(16.dp),
            color = Color(0xFF3B82F6)
          ) {}
        }
        if (prPct > 0) {
          Surface(
            modifier = Modifier.weight(prPct.coerceAtLeast(0.01f)).height(16.dp),
            color = Color(0xFFF59E0B)
          ) {}
        }
        if (blPct > 0) {
          Surface(
            modifier = Modifier.weight(blPct.coerceAtLeast(0.01f)).height(16.dp),
            color = Color(0xFFEC4899)
          ) {}
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Badges
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      LoanLegendItem(Color(0xFF10B981), AppStrings.get("down_payment", language), downPayment)
      LoanLegendItem(Color(0xFF3B82F6), if (language == AppLanguage.ARABIC) "أصل التمويل" else "Principal", financedPrincipal)
      LoanLegendItem(Color(0xFFF59E0B), if (language == AppLanguage.ARABIC) "الأرباح" else "Interest", totalProfit)
      LoanLegendItem(Color(0xFFEC4899), AppStrings.get("balloon_payment", language), balloonPayment)
    }
  }
}

@Composable
private fun LoanLegendItem(color: Color, title: String, amount: Double) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = color) {}
      Spacer(modifier = Modifier.width(4.dp))
      Text(text = title, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
    }
    Text(
      text = "${String.format("%.0f", amount)}",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

// ----------------------------------------------------
// Recharts-Style Interactive Monthly Breakdown Chart
// ----------------------------------------------------

data class MonthBreakdownItem(
  val monthKey: String,
  val monthLabel: String,
  val expenses: Double,
  val installments: Double,
  val savings: Double
)

@Composable
fun RechartsStyleMonthlyBarChart(
  data: List<MonthBreakdownItem>,
  language: AppLanguage,
  isFirestoreSource: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedIndex by androidx.compose.runtime.remember(data) {
    androidx.compose.runtime.mutableStateOf(data.lastIndex.coerceAtLeast(0))
  }

  val maxVal = data.maxOfOrNull {
    maxOf(it.expenses, it.installments, it.savings)
  }?.coerceAtLeast(500.0) ?: 500.0

  val expenseColor = Color(0xFFF43F5E) // Coral
  val installmentColor = Color(0xFF8B5CF6) // Purple
  val savingsColor = Color(0xFF10B981) // Emerald

  androidx.compose.material3.Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = androidx.compose.material3.CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Top Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = AppStrings.get("monthly_breakdown_title", language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = AppStrings.get("monthly_breakdown_subtitle", language),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Source badge & refresh button
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isFirestoreSource) Color(0xFF0284C7).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.padding(start = 6.dp)
        ) {
          Row(
            modifier = Modifier
              .clickable(onClick = onRefresh)
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              modifier = Modifier.size(7.dp),
              shape = CircleShape,
              color = if (isFirestoreSource) Color(0xFF0284C7) else Color(0xFF10B981)
            ) {}
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isFirestoreSource) AppStrings.get("source_firestore", language) else AppStrings.get("source_local", language),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = if (isFirestoreSource) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Recharts Legend Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        RechartsLegendPill(color = expenseColor, label = AppStrings.get("expenses_legend", language))
        RechartsLegendPill(color = installmentColor, label = AppStrings.get("installments_legend", language))
        RechartsLegendPill(color = savingsColor, label = AppStrings.get("savings_legend", language))
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Chart Canvas & Bars
      if (data.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = AppStrings.get("no_breakdown_data", language),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        // Bar Chart Area
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
        ) {
          // Horizontal subtle grid lines
          Canvas(modifier = Modifier.matchParentSize()) {
            val gridColor = Color.Gray.copy(alpha = 0.15f)
            val strokeW = 1.dp.toPx()
            for (i in 0..4) {
              val y = size.height * (i / 4f)
              drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeW
              )
            }
          }

          // Bars Row
          Row(
            modifier = Modifier
              .matchParentSize()
              .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            data.forEachIndexed { index, item ->
              val isSelected = index == selectedIndex
              val expRatio = (item.expenses / maxVal).toFloat().coerceIn(0f, 1f)
              val instRatio = (item.installments / maxVal).toFloat().coerceIn(0f, 1f)
              val savRatio = (item.savings / maxVal).toFloat().coerceIn(0f, 1f)

              val animatedExp by animateFloatAsState(
                targetValue = expRatio,
                animationSpec = tween(durationMillis = 650),
                label = "expBar_$index"
              )
              val animatedInst by animateFloatAsState(
                targetValue = instRatio,
                animationSpec = tween(durationMillis = 650),
                label = "instBar_$index"
              )
              val animatedSav by animateFloatAsState(
                targetValue = savRatio,
                animationSpec = tween(durationMillis = 650),
                label = "savBar_$index"
              )

              Column(
                modifier = Modifier
                  .weight(1f)
                  .clickable { selectedIndex = index }
                  .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
              ) {
                // Grouped 3 Bars
                Row(
                  modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(3.dp),
                  verticalAlignment = Alignment.Bottom
                ) {
                  // Expense Bar
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .height((150.dp * animatedExp).coerceAtLeast(if (item.expenses > 0) 6.dp else 2.dp))
                      .background(
                        color = if (isSelected) expenseColor else expenseColor.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                      )
                  )
                  // Installment Bar
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .height((150.dp * animatedInst).coerceAtLeast(if (item.installments > 0) 6.dp else 2.dp))
                      .background(
                        color = if (isSelected) installmentColor else installmentColor.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                      )
                  )
                  // Savings Bar
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .height((150.dp * animatedSav).coerceAtLeast(if (item.savings > 0) 6.dp else 2.dp))
                      .background(
                        color = if (isSelected) savingsColor else savingsColor.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                      )
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Month Label
                Text(
                  text = item.monthLabel,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recharts Interactive Tooltip Box for Selected Month
        if (selectedIndex in data.indices) {
          val selected = data[selectedIndex]
          val totalOutflow = selected.expenses + selected.installments
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = selected.monthLabel,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${AppStrings.get("currency", language)} ${String.format("%,.0f", totalOutflow + selected.savings)}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                BreakdownStatItem(
                  color = expenseColor,
                  label = AppStrings.get("expenses_legend", language),
                  amount = selected.expenses,
                  language = language
                )
                BreakdownStatItem(
                  color = installmentColor,
                  label = AppStrings.get("installments_legend", language),
                  amount = selected.installments,
                  language = language
                )
                BreakdownStatItem(
                  color = savingsColor,
                  label = AppStrings.get("savings_legend", language),
                  amount = selected.savings,
                  language = language
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RechartsLegendPill(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Surface(
      modifier = Modifier.size(9.dp),
      shape = CircleShape,
      color = color
    ) {}
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun BreakdownStatItem(
  color: Color,
  label: String,
  amount: Double,
  language: AppLanguage
) {
  Column {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Surface(
        modifier = Modifier.size(6.dp),
        shape = CircleShape,
        color = color
      ) {}
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Text(
      text = "${String.format("%,.0f", amount)} ${AppStrings.get("currency", language)}",
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

