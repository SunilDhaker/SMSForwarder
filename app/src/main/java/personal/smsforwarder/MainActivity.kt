package personal.smsforwarder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import personal.smsforwarder.ui.screens.*
import personal.smsforwarder.ui.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS Forwarder") }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Rules") },
                    label = { Text("Rules") },
                    selected = currentRoute == "rules",
                    onClick = { navController.navigate("rules") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "History") },
                    label = { Text("History") },
                    selected = currentRoute == "history",
                    onClick = { navController.navigate("history") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Build, contentDescription = "Tester") },
                    label = { Text("Tester") },
                    selected = currentRoute == "tester",
                    onClick = { navController.navigate("tester") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Logs") },
                    label = { Text("Logs") },
                    selected = currentRoute == "logs",
                    onClick = { navController.navigate("logs") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Reliability") },
                    label = { Text("Reliability") },
                    selected = currentRoute == "reliability",
                    onClick = { navController.navigate("reliability") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "rules",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("rules") { RuleListScreen(navController, viewModel) }
            composable("history") { ForwardingHistoryScreen(viewModel, navController) }
            composable("tester") {
                val rules by viewModel.allRules.collectAsState()
                RegexTesterScreen(rules = rules)
            }
            composable("tester/{sender}/{body}") { backStackEntry ->
                val sender = backStackEntry.arguments?.getString("sender") ?: ""
                val body = backStackEntry.arguments?.getString("body") ?: ""
                val rules by viewModel.allRules.collectAsState()
                RegexTesterScreen(
                    initialSender = java.net.URLDecoder.decode(sender, "UTF-8"),
                    initialBody = java.net.URLDecoder.decode(body, "UTF-8"),
                    rules = rules
                )
            }
            composable("logs") { LogsScreen(viewModel, navController) }
            composable("reliability") { ReliabilityScreen() }
            composable("rule_editor/{ruleId}") { backStackEntry ->
                val ruleId = backStackEntry.arguments?.getString("ruleId")?.toLongOrNull() ?: 0L
                RuleEditorScreen(navController, viewModel, ruleId)
            }
        }
    }
}
