package personal.smsforwarder.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import personal.smsforwarder.receiver.BootReceiver
import personal.smsforwarder.service.ForwarderForegroundService

@Composable
fun ReliabilityScreen() {
    val context = LocalContext.current
    var serviceRunning by remember { mutableStateOf(ForwarderForegroundService.isRunning(context)) }
    var serviceEnabled by remember { mutableStateOf(BootReceiver.isServiceEnabled(context)) }
    var batteryOptimized by remember { mutableStateOf(isBatteryOptimized(context)) }

    // Refresh state when screen is shown
    LaunchedEffect(Unit) {
        serviceRunning = ForwarderForegroundService.isRunning(context)
        serviceEnabled = BootReceiver.isServiceEnabled(context)
        batteryOptimized = isBatteryOptimized(context)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Reliability Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Configure these settings to ensure the app runs reliably in the background.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Foreground Service Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (serviceRunning)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Foreground Service",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (serviceRunning) "Running - App will stay active"
                            else "Stopped - App may be killed by OS",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (serviceRunning)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = if (serviceRunning) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (serviceRunning) Color(0xFF4CAF50) else Color(0xFFFFA000)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (serviceRunning) {
                                ForwarderForegroundService.stop(context)
                                BootReceiver.setServiceEnabled(context, false)
                            } else {
                                ForwarderForegroundService.start(context)
                                BootReceiver.setServiceEnabled(context, true)
                            }
                            serviceRunning = !serviceRunning
                            serviceEnabled = BootReceiver.isServiceEnabled(context)
                        },
                        colors = if (serviceRunning)
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        else
                            ButtonDefaults.buttonColors()
                    ) {
                        Text(if (serviceRunning) "Stop Service" else "Start Service")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = serviceEnabled,
                        onCheckedChange = { enabled ->
                            BootReceiver.setServiceEnabled(context, enabled)
                            serviceEnabled = enabled
                            if (enabled && !serviceRunning) {
                                ForwarderForegroundService.start(context)
                                serviceRunning = true
                            }
                        }
                    )
                    Text(
                        "Start automatically on boot",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Battery Optimization Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (!batteryOptimized)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Battery Optimization",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (!batteryOptimized) "Disabled - App won't be restricted"
                            else "Enabled - OS may kill the app",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (!batteryOptimized)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Icon(
                        imageVector = if (!batteryOptimized) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (!batteryOptimized) Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        requestBatteryOptimizationExemption(context)
                    },
                    enabled = batteryOptimized
                ) {
                    Text(if (batteryOptimized) "Disable Battery Optimization" else "Already Disabled")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SMS Permissions Card
        ChecklistItem(
            title = "SMS Permissions",
            description = "Required to receive and forward SMS.",
            buttonText = "Open App Settings",
            onClick = { openAppSettings(context) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // General Battery Settings Card
        ChecklistItem(
            title = "Additional Battery Settings",
            description = "Some devices have extra battery restrictions. Check manufacturer settings.",
            buttonText = "Open Battery Settings",
            onClick = { openBatterySettings(context) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Info text
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Tips for best reliability:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "1. Keep the foreground service running\n" +
                    "2. Disable battery optimization for this app\n" +
                    "3. On some devices (Xiaomi, Huawei, Samsung), check for additional battery/power management settings\n" +
                    "4. Lock the app in recent apps (if supported)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ChecklistItem(title: String, description: String, buttonText: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}

private fun isBatteryOptimized(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return !pm.isIgnoringBatteryOptimizations(context.packageName)
}

private fun requestBatteryOptimizationExemption(context: Context) {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    context.startActivity(intent)
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

private fun openBatterySettings(context: Context) {
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    context.startActivity(intent)
}
