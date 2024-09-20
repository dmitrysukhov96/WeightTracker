package com.dmitrysukhov.weighttracker

import AddWeightDialog
import android.util.Log
import androidx.compose.foundation.clickable
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
    val startOfMonth = currentMonth.toDate().time // Начало месяца
    val endOfMonth = currentMonth.dayOfMonth().withMaximumValue()
        .toDate().time + (1000 * 60 * 60 * 24) // Конец месяца
    var showAddWeightDialog by remember { mutableStateOf(false) }
    var selectedEntry: WeightEntry? by remember { mutableStateOf(null) }
    if (showAddWeightDialog) {
        AddWeightDialog(
            viewModel = viewModel,
            onDismiss = { showAddWeightDialog = false },
            selectedEntry = selectedEntry
        )
    }
    val weightEntries by viewModel.getWeightEntriesForMonth(start = startOfMonth, end = endOfMonth)
        .collectAsState(emptyList())
    Log.d("WeightTracker", "Weight Entries: $weightEntries")
    if (showAddWeightDialog) {
        AddWeightDialog(
            viewModel = viewModel,
            onDismiss = { showAddWeightDialog = false },
            selectedEntry = selectedEntry
        )
    }
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
                val isToday = entryDate == now()
                Log.d("dimaaa", "Raw date: ${entry.date}, Formatted: $entryDate")
                val formattedDate = entryDate.toString("dd MMM yyyy")
                val sugarText = if (entry.noSugar) "No Sugar" else "Has Sugar"
                val breadText = if (entry.noBread) "No Bread" else "Has Bread"
                Text(
                    text = "Date: $formattedDate, Weight: ${entry.weight}, Grams: ${entry.grams}, $sugarText, $breadText",
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (isToday) 20.sp else 16.sp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            showAddWeightDialog = true
                            selectedEntry = entry
                        }
                )
            }
        }
    }
}