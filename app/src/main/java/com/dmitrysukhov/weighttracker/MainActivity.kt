package com.dmitrysukhov.weighttracker

import AddWeightDialog
import WeightGoalDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.W900
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dmitrysukhov.weighttracker.ui.theme.WeightTrackerTheme
import org.joda.time.DateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WeightTrackerTheme { WeightTrackerApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrackerApp() {
    val context = LocalContext.current
    val viewModel = viewModel<WeightViewModel>()
    val navController = rememberNavController()
    val title by remember { mutableStateOf("Weight Tracker") }
    var showAddWeightDialog by remember { mutableStateOf(false) }
    if (showAddWeightDialog) AddWeightDialog(
        viewModel = viewModel, onDismiss = { showAddWeightDialog = false }, selectedEntry = null
    )
    val savedGoal = loadWeightGoal(context)
    var showGoalDialog by remember { mutableStateOf(false) }
    if (showGoalDialog) WeightGoalDialog(context, onDismiss = { showGoalDialog = false })
    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) }, floatingActionButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (savedGoal!=null)"Цель: ${savedGoal.goalWeight} до ${DateTime(savedGoal.targetDate).toString("d.MM.yyyy")}"
                         else "", fontSize = 18.sp, fontWeight = W900)
                Spacer(modifier = Modifier.width(16.dp))
                FloatingActionButton(onClick = { showGoalDialog = true }) {
                    Image(painterResource(R.drawable.goal), contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(onClick = { showAddWeightDialog = true })
                { Text("+", fontSize = 30.sp) }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, startDestination = MONTH_SCREEN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ABOUT_PROJECT_SCREEN) { AboutProjectScreen() }
            composable(MONTH_SCREEN) { MonthView(viewModel) }
        }
    }
}

@Composable
fun AboutProjectScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("About Project Screen")
    }
}

const val MONTH_SCREEN = "month_view"
const val ABOUT_PROJECT_SCREEN = "about_project"
