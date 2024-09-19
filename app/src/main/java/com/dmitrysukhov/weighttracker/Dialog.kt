package com.dmitrysukhov.weighttracker

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.joda.time.LocalDate
import java.util.Calendar

@Composable
fun AddWeightDialog(viewModel: WeightViewModel, onDismiss: () -> Unit) {
    var weight by remember { mutableStateOf("") }
    var grams by remember { mutableStateOf("") }
    var noSugar by remember { mutableStateOf(false) }
    var noBread by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePicker(
            initialDate = selectedDate,
            onDateSelected = {
                selectedDate = it
                showDatePicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Weight Entry") },
        text = {
            Column {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = grams,
                    onValueChange = { grams = it },
                    label = { Text("Grams") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = noSugar, onCheckedChange = { noSugar = it })
                    Text("No Sugar")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = noBread, onCheckedChange = { noBread = it })
                    Text("No Bread")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showDatePicker = true }) {
                    Text("Select Date: ${selectedDate.toString("yyyy-MM-dd")}")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val entry = WeightEntry(
                    date = selectedDate.toDate().time / (1000 * 60 * 60 * 24),
                    weight = weight.toFloatOrNull() ?: 0f,
                    noSugar = noSugar,
                    noBread = noBread,
                    grams = grams.toIntOrNull() ?: 0
                )
                viewModel.insertWeight(entry)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DatePicker(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        set(initialDate.year, initialDate.monthOfYear - 1, initialDate.dayOfMonth)
    }

    LaunchedEffect(context) {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, day ->
                val newDate = LocalDate(year, month + 1, day)
                onDateSelected(newDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}
