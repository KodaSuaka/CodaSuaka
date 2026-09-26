package com.example.codasuaka.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.codasuaka.ui.theme.OnSurfaceVariant
import com.example.codasuaka.ui.theme.Primary
import com.example.codasuaka.ui.theme.Secondary
import com.example.codasuaka.ui.theme.Surface
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Komponen Kalender Terpadu Coda Suaka.
 * Digunakan sebagai standar global untuk pemilihan tanggal di seluruh aplikasi.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodaSuakaDatePickerDialog(
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(),
        initialDisplayedMonthMillis = initialDate.withDayOfMonth(1).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    )
    
    var showYearPicker by remember { mutableStateOf(false) }
    val locale = remember { Locale("id", "ID") }
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", locale) }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val selectedDate = Instant.ofEpochMilli(it)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate()
                    onDateSelected(selectedDate)
                }
            }) {
                Text("Pilih", fontWeight = FontWeight.ExtraBold, color = Secondary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = OnSurfaceVariant)
            }
        },
        colors = DatePickerDefaults.colors(containerColor = Surface)
    ) {
        if (showYearPicker) {
            val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate()
                
            YearPickerDialog(
                selectedYear = displayMonth.year,
                onYearSelected = { year ->
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = datePickerState.displayedMonthMillis
                        set(Calendar.YEAR, year)
                    }
                    datePickerState.displayedMonthMillis = cal.timeInMillis
                    showYearPicker = false
                },
                onDismiss = { showYearPicker = false }
            )
        }

        Column(modifier = Modifier.padding(top = 16.dp)) {
            // Header Navigasi Kustom
            val displayMonth = Instant.ofEpochMilli(datePickerState.displayedMonthMillis)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate()
            
            val monthTitle = remember(displayMonth) { displayMonth.format(formatter) }
            
            CustomCalendarNavigation(
                title = monthTitle.replaceFirstChar { it.uppercase() },
                onPrevClick = {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = datePickerState.displayedMonthMillis
                        add(Calendar.MONTH, -1)
                    }
                    datePickerState.displayedMonthMillis = cal.timeInMillis
                },
                onNextClick = {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = datePickerState.displayedMonthMillis
                        add(Calendar.MONTH, 1)
                    }
                    datePickerState.displayedMonthMillis = cal.timeInMillis
                },
                onTitleClick = { showYearPicker = true },
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clipToBounds()
            ) {
                DatePicker(
                    state = datePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = Surface,
                        titleContentColor = Secondary,
                        headlineContentColor = Secondary,
                        weekdayContentColor = Secondary.copy(alpha = 0.6f),
                        subheadContentColor = Secondary.copy(alpha = 0.6f),
                        yearContentColor = Secondary.copy(alpha = 0.7f),
                        currentYearContentColor = Primary,
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = Primary,
                        dayContentColor = Secondary,
                        selectedDayContentColor = Color.White,
                        selectedDayContainerColor = Primary,
                        todayContentColor = Primary,
                        todayDateBorderColor = Primary
                    ),
                    modifier = Modifier.offset(y = (-48).dp)
                )
            }
        }
    }
}
