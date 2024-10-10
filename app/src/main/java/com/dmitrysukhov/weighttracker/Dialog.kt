import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dmitrysukhov.weighttracker.WeightEntry
import com.dmitrysukhov.weighttracker.WeightGoal
import com.dmitrysukhov.weighttracker.WeightViewModel
import com.dmitrysukhov.weighttracker.deleteWeightGoal
import com.dmitrysukhov.weighttracker.loadWeightGoal
import com.dmitrysukhov.weighttracker.saveWeightGoal
import org.joda.time.LocalDate
import java.util.Calendar

@Composable
fun AddWeightDialog(
    viewModel: WeightViewModel, onDismiss: () -> Unit, selectedEntry: WeightEntry? = null
) {
    var weight by rememberSaveable { mutableStateOf(selectedEntry?.weight?.toString() ?: "") }
    var grams by rememberSaveable { mutableStateOf(selectedEntry?.grams?.toString() ?: "") }
    var noSugar by rememberSaveable { mutableStateOf(selectedEntry?.noSugar ?: false) }
    var noBread by rememberSaveable { mutableStateOf(selectedEntry?.noBread ?: false) }
    var failedDiet by rememberSaveable { mutableStateOf(selectedEntry?.failedDiet ?: false) }
    var selectedDate by rememberSaveable {
        mutableStateOf(selectedEntry?.let { LocalDate(it.date) } ?: LocalDate.now())
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    if (showDatePicker) DatePicker(
        initialDate = selectedDate,
        onDateSelected = { selectedDate = it; showDatePicker = false }
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add/Update Weight Entry") },
        text = {
            Column {
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it }, label = { Text("Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = grams, onValueChange = { grams = it }, label = { Text("Grams") },
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = failedDiet, onCheckedChange = { failedDiet = it })
                    Text("Failed Diet")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showDatePicker = true }) {
                    Text("Select Date: ${selectedDate.toString("yyyy-MM-dd")}")
                }
                selectedEntry?.let {
                    Button(onClick = { viewModel.deleteWeight(it); onDismiss() }
                    ) { Text("Delete") }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val entry = WeightEntry(
                    id = selectedEntry?.id ?: 0, date = selectedDate.toDate().time,
                    weight = weight.replace(",",".").toFloatOrNull() ?: 0f, noSugar = noSugar, noBread = noBread,
                    grams = grams.toIntOrNull() ?: 0, failedDiet = failedDiet
                )
                viewModel.insertWeight(entry)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DatePicker(initialDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
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
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}

@Composable
fun WeightGoalDialog(context: Context, onDismiss: () -> Unit) {
    var currentWeight by rememberSaveable { mutableStateOf("") }
    var goalWeight by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    // Загрузка данных из Shared Preferences, если они есть
    val savedGoal = loadWeightGoal(context)
    savedGoal?.let {
        currentWeight = it.currentWeight.toString()
        goalWeight = it.goalWeight.toString()
        selectedDate = LocalDate(it.targetDate)
    }

    if (showDatePicker) {
        DatePicker(
            initialDate = selectedDate,
            onDateSelected = { selectedDate = it; showDatePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Установка цели") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentWeight,
                    onValueChange = { currentWeight = it },
                    label = { Text("Текущий вес") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = goalWeight,
                    onValueChange = { goalWeight = it },
                    label = { Text("Целевой вес") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Button(onClick = { showDatePicker = true }) {
                    Text("Выбрать дату: ${selectedDate.toString("yyyy-MM-dd")}")
                }
                savedGoal?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        deleteWeightGoal(context)
                        onDismiss()
                    }) {
                        Text("Удалить цель")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val goal = WeightGoal(
                    currentWeight = currentWeight.toFloatOrNull() ?: 0f,
                    goalWeight = goalWeight.toFloatOrNull() ?: 0f,
                    startDate = LocalDate.now().toDate().time,
                    targetDate = selectedDate.toDate().time
                )
                saveWeightGoal(context, goal)
                onDismiss()
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
