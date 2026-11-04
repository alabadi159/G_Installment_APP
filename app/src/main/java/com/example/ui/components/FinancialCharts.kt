package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
