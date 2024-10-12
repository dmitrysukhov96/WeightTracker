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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.LocalDate.now
import org.joda.time.Months

@Composable
fun MonthView(viewModel: WeightViewModel) {
    var currentMonth by remember { mutableStateOf(now().withDayOfMonth(1)) }
    val startOfMonth = currentMonth.toDate().time
    val endOfMonth = currentMonth.dayOfMonth().withMaximumValue().toDate().time + (1000 * 60 * 60 * 24)
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
    val context = LocalContext.current
    val savedGoal = loadWeightGoal(context)
    Image(
        contentScale = ContentScale.FillHeight, painter = painterResource(R.drawable.img),
        contentDescription = null, modifier = Modifier.fillMaxSize().alpha(0.75F)
    )
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
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp
            )
            ElevatedButton({ currentMonth = currentMonth.plus(Months.ONE) }) { Text(">") }
        }

        LazyColumn(modifier = Modifier.padding(bottom = 80.dp)) {
            val daysInMonth = currentMonth.dayOfMonth().withMaximumValue().dayOfMonth
            items(daysInMonth) { dayIndex ->
                val currentDate = currentMonth.withDayOfMonth(dayIndex + 1)
                val entryForDay = weightEntries.find { LocalDate(it.date).isEqual(currentDate) }
                if (entryForDay != null || (savedGoal != null && (currentDate.isAfter(LocalDate(savedGoal.startDate)) && currentDate.isBefore(LocalDate(savedGoal.targetDate))))) {
                    val predictedWeight = if (currentDate.isAfter(LocalDate(savedGoal?.startDate)) && currentDate.isBefore(LocalDate(savedGoal?.targetDate))) {
                        calculatePredictedWeight(
                            currentDate = currentDate,
                            startDate = LocalDate(savedGoal!!.startDate),
                            startWeight = savedGoal.currentWeight,
                            endDate = LocalDate(savedGoal.targetDate),
                            goalWeight = savedGoal.goalWeight
                        )
                    } else null
                    val sugarIcon = entryForDay?.let {
                        if (it.noSugar) painterResource(R.drawable.no_sugar) else painterResource(R.drawable.sugar)
                    }
                    val breadIcon = entryForDay?.let {
                        if (it.noBread) painterResource(R.drawable.no_bread) else painterResource(R.drawable.bread)
                    }
                    val previousEntry = weightEntries.getOrNull(weightEntries.indexOf(entryForDay) - 1)
                    val weightChangeIcon = when {
                        previousEntry != null && entryForDay != null && entryForDay.weight > previousEntry.weight -> painterResource(R.drawable.arrow_up)
                        previousEntry != null && entryForDay != null && entryForDay.weight < previousEntry.weight -> painterResource(R.drawable.arrow_down)
                        else -> null
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp, 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.inversePrimary)
                            .padding(4.dp)
                            .height(36.dp)
                            .clickable {
                                selectedEntry = entryForDay
                                showAddWeightDialog = true
                            }
                    ) {
                        Row(Modifier.weight(1F), horizontalArrangement = Arrangement.Center) {
                            Text(currentDate.toString("d MMM"))
                        }

                        Row(
                            Modifier
                                .fillMaxHeight()
                                .weight(2F)
                                .background(Color.White.copy(0.3f)),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (weightChangeIcon != null) Image(
                                painter = weightChangeIcon,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(24.dp, 24.dp)
                            ) else Spacer(modifier = Modifier.size(24.dp, 24.dp))

                            Text(
                                if (entryForDay != null) stringResource(R.string.weight_arg, entryForDay.weight) else "              ",
                                fontWeight = W700
                            )
                            Text(
                                if (predictedWeight != null) "%.1f".format(predictedWeight) else "          ",
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.alpha(0.7f).padding(end = 16.dp)
                            )
                        }

                        Row(
                            Modifier.weight(1F),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(8.dp))
                            if (breadIcon != null) Image(
                                painter = breadIcon, contentDescription = null,
                                Modifier.size(30.dp)
                            )
                            if (sugarIcon != null) Image(
                                painter = sugarIcon, contentDescription = null,
                                Modifier.size(30.dp)
                            )
                        }

                        Row(
                            Modifier.weight(1F),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (entryForDay?.failedDiet == true)
                                Image(painterResource(R.drawable.idk), null, Modifier.size(36.dp))
                            else Text(
                                stringResource(R.string.grams_arg, entryForDay?.grams ?: 0),
                                fontWeight = W700
                            )
                        }
                    }
                }
            }
        }
    }
}

fun calculatePredictedWeight(
    currentDate: LocalDate, startDate: LocalDate, startWeight: Float, endDate: LocalDate,
    goalWeight: Float
): Float {
    val totalDays = Days.daysBetween(startDate, endDate).days
    val daysPassed = Days.daysBetween(startDate, currentDate).days
    val weightChangePerDay = (goalWeight - startWeight) / totalDays
    return startWeight + weightChangePerDay * daysPassed
}
