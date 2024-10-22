import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dmitrysukhov.weighttracker.R
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
    viewModel: WeightViewModel, onDismiss: () -> Unit, date: Long? = null
) {
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate(date)) }
    val entry by viewModel.getWeightEntryForDate(selectedDate.toDate().time)
        .collectAsStateWithLifecycle(null)
    var weight by rememberSaveable { mutableStateOf("") }
    var grams by rememberSaveable { mutableStateOf("") }
    var plusGrams by rememberSaveable { mutableStateOf("") }
    var noSugar by rememberSaveable { mutableStateOf(false) }
    var noBread by rememberSaveable { mutableStateOf(false) }
    var failedDiet by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(entry) {
        weight = if (entry?.weight != null) entry?.weight.toString() else ""
        grams = if (entry?.grams != null) entry?.grams.toString() else ""
        noSugar = entry?.noSugar ?: false
        noBread = entry?.noBread ?: false
        failedDiet = entry?.failedDiet ?: false
        entry?.date?.let { selectedDate = LocalDate(it) }
    }
    if (showDatePicker) DatePicker(
        initialDate = selectedDate, onDateSelected = { selectedDate = it; showDatePicker = false }
    )
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.Black,
        title = { Text(stringResource(R.string.add_weight_data)) },
        text = {
            Column {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.weight)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Row {
                    OutlinedTextField(
                        value = grams, onValueChange = { grams = it },
                        label = { Text(stringResource(R.string.grams)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(2f)
                    )
                    Spacer(Modifier.width(16.dp))
                    OutlinedTextField(
                        value = plusGrams, onValueChange = { plusGrams = it },
                        label = { Text(stringResource(R.string.plus)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1F)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(32.dp)
                ) {
                    Checkbox(checked = noSugar, onCheckedChange = { noSugar = it })
                    Text(stringResource(R.string.no_sugar))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(32.dp)
                ) {
                    Checkbox(checked = noBread, onCheckedChange = { noBread = it })
                    Text(stringResource(R.string.no_bread))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(32.dp)
                ) {
                    Checkbox(checked = failedDiet, onCheckedChange = { failedDiet = it })
                    Text(stringResource(R.string.failed_diet))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showDatePicker = true }) {
                    Text(stringResource(R.string.date) + ": ${selectedDate.toString("dd.MM.yyyy")}")
                }
                entry?.let {
                    Button(onClick = { viewModel.deleteWeight(it); onDismiss() }
                    ) { Text(stringResource(R.string.delete)) }
                }
            }
        }, confirmButton = {
            Button(onClick = {
                val newEntry = WeightEntry(
                    id = entry?.id ?: 0, date = selectedDate.toDate().time,
                    weight = weight.replace(",", ".").toFloatOrNull() ?: 0f,
                    noSugar = noSugar, noBread = noBread, failedDiet = failedDiet,
                    grams = (grams.toIntOrNull() ?: 0) + (plusGrams.toIntOrNull() ?: 0),
                )
                viewModel.insertWeight(newEntry)
                onDismiss()
            }) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = { Button(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } }
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
            android.R.style.Theme_DeviceDefault_Dialog, { _, year, month, day ->
                val newDate = LocalDate(year, month + 1, day)
                onDateSelected(newDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
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
        onDismissRequest = onDismiss, containerColor = Color.Black,
        title = { Text(stringResource(R.string.set_goal)) }, text = {
            Column {
                OutlinedTextField(
                    value = currentWeight, onValueChange = { currentWeight = it },
                    label = { Text(stringResource(R.string.current_weight)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = goalWeight, onValueChange = { goalWeight = it },
                    label = { Text(stringResource(R.string.goal_weight)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Button(onClick = { showDatePicker = true }) {
                    Text(stringResource(R.string.choose_date) + ": ${selectedDate.toString("yyyy-MM-dd")}")
                }
                savedGoal?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        deleteWeightGoal(context)
                        onDismiss()
                    }) { Text(stringResource(R.string.remove_goal)) }
                }
            }
        }, confirmButton = {
            Button(onClick = {
                val goal = WeightGoal(
                    currentWeight = currentWeight.toFloatOrNull() ?: 0f,
                    goalWeight = goalWeight.toFloatOrNull() ?: 0f,
                    startDate = LocalDate.now().toDate().time,
                    targetDate = selectedDate.toDate().time
                )
                saveWeightGoal(context, goal); onDismiss()
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { Button(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } }
    )
}

@Composable
fun AddFoodDialog(viewModel: WeightViewModel, onDismiss: () -> Unit) {
    var foodGrams by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.Black,
        title = { Text(stringResource(R.string.add_food)) }, text = {
            Column {
                OutlinedTextField(
                    value = foodGrams,
                    onValueChange = { foodGrams = it },
                    label = { Text(stringResource(R.string.grams)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }, confirmButton = {
            Button(onClick = {
                val gramsValue = foodGrams.toIntOrNull() ?: 0
                viewModel.addFoodToWeightEntry(gramsValue); onDismiss()
            }) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = { Button(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } }
    )
}
