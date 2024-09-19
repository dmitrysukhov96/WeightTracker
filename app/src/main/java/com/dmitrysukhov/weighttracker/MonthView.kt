package com.dmitrysukhov.weighttracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.joda.time.LocalDate
import org.joda.time.LocalDate.now
import org.joda.time.Months

@Composable
fun MonthView(viewModel: WeightViewModel) {
    var currentMonth by remember { mutableStateOf(now().withDayOfMonth(1)) }
    val endOfMonth = remember { currentMonth.dayOfMonth().withMaximumValue() }
    val weightEntries by viewModel.getWeightEntriesForMonth(
        start = currentMonth.toDate().time / (1000 * 60 * 60 * 24),
        end = endOfMonth.toDate().time / (1000 * 60 * 60 * 24)
    ).collectAsState(emptyList())
    Column(Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Button(onClick = {
                currentMonth = currentMonth.minus(Months.ONE)
            }) {
                Text("<")
            }
            Text(
                "${currentMonth.monthOfYear().getAsText()} ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium
            )
            Button(onClick = {
                currentMonth = currentMonth.plus(Months.ONE)
            }) {
                Text(">")
            }
        }
        LazyColumn {
            items(weightEntries) { entry ->
                val entryDate = LocalDate(entry.date)
                val isToday = entryDate == now().toDateMidnight().toLocalDate()
                Text(
                    text = "Weight: ${entry.weight}, Grams: ${entry.grams}",
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (isToday) 20.sp else 16.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}