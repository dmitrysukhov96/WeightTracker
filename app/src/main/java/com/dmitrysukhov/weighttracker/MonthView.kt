package com.dmitrysukhov.weighttracker

import AddWeightDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W700
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            ElevatedButton({ currentMonth = currentMonth.minus(Months.ONE) }) { Text("<") }
            Text(
                currentMonth.toString("MMMM yyyy"),
                style = MaterialTheme.typography.titleMedium, fontSize = 18.sp
            )
            ElevatedButton({ currentMonth = currentMonth.plus(Months.ONE) }) { Text(">") }
        }
        LazyColumn {
            items(weightEntries.size) { index ->
                val entry = weightEntries[index]
                val entryDate = LocalDate(entry.date)
                val formattedDate = entryDate.toString("dd MMM")
                val sugarIcon =
                    if (entry.noSugar) painterResource(R.drawable.no_sugar) else painterResource(R.drawable.sugar)
                val breadIcon =
                    if (entry.noBread) painterResource(R.drawable.no_bread) else painterResource(R.drawable.bread)
                val previousEntry = weightEntries.getOrNull(index - 1)
                val weightChangeIcon = when {
                    previousEntry != null && entry.weight > previousEntry.weight -> painterResource(
                        R.drawable.arrow_up
                    )

                    previousEntry != null && entry.weight < previousEntry.weight -> painterResource(
                        R.drawable.arrow_down
                    )

                    else -> null
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.inversePrimary)
                        .padding(4.dp)
                        .clickable {
                            selectedEntry = entry
                            showAddWeightDialog = true
                        }
                ) {
                    Row(Modifier.weight(1.3F), horizontalArrangement = Arrangement.Center) {
                        Text(formattedDate)
                    }
                    Row(
                        Modifier
                            .fillMaxHeight()
                            .weight(1.2F)
                            .background(Color.White.copy(alpha = 0.2f)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (weightChangeIcon != null) Image(
                            painter = weightChangeIcon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp, 24.dp)
                        ) else Spacer(modifier = Modifier.size(24.dp, 24.dp))
                        Text(stringResource(R.string.weight_arg, entry.weight), fontWeight = W700)
                        Spacer(modifier = Modifier.size(24.dp, 24.dp))
                    }
                    Row(
                        Modifier.weight(1F), horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(painter = breadIcon, contentDescription = null, Modifier.size(36.dp))
                        Image(painter = sugarIcon, contentDescription = null, Modifier.size(36.dp))
                    }
                    Row(
                        Modifier.weight(1F),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (entry.failedDiet)
                            Image(painterResource(R.drawable.idk), null, Modifier.size(36.dp))
                        else Text(stringResource(R.string.grams_arg, entry.grams), fontWeight = W700)
                    }
                }
            }
        }
    }
}