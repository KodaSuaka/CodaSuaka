package com.example.codasuaka.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.codasuaka.ui.theme.*

/**
 * Reusable header component for sections with navigation (prev/next) and clickable month-year title.
 */
@Composable
fun SectionHeaderNavigation(
    title: String,
    monthYearText: String,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onMonthYearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label/Title on the left
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface
        )

        // Pill-Shaped Navigation on the right
        Surface(
            modifier = Modifier
                .width(180.dp) // Memanjang secara horizontal
                .height(38.dp), // Ramping secara vertikal
            shape = RoundedCornerShape(100.dp), // Full pill shape
            color = Primary.copy(alpha = 0.08f), // Shading biru muda sangat halus
            border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Arrow
                IconButton(
                    onClick = onPrevClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Sebelumnya",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Clickable Month Year Text in the middle
                Text(
                    text = monthYearText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onMonthYearClick() }
                )

                // Right Arrow
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Berikutnya",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
