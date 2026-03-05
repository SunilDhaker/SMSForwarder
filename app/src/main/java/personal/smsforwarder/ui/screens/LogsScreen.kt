package personal.smsforwarder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import personal.smsforwarder.data.db.entities.ForwardLog
import personal.smsforwarder.data.db.entities.SmsEvent
import personal.smsforwarder.ui.viewmodel.AppViewModel
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogsScreen(viewModel: AppViewModel, navController: NavController) {
    val events by viewModel.allEvents.collectAsState()
    val logs by viewModel.allLogs.collectAsState()
    val logsByEventId = remember(logs) { logs.groupBy { it.smsEventId } }

    if (events.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No messages received yet.")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(events) { event ->
                val eventLogs = logsByEventId[event.id].orEmpty()
                EventLogItem(
                    event = event,
                    logs = eventLogs,
                    onTestClick = {
                        val encodedSender = URLEncoder.encode(event.sender, "UTF-8")
                        val encodedBody = URLEncoder.encode(event.body, "UTF-8")
                        navController.navigate("tester/$encodedSender/$encodedBody")
                    }
                )
            }
        }
    }
}

@Composable
fun EventLogItem(
    event: SmsEvent,
    logs: List<ForwardLog>,
    onTestClick: () -> Unit
) {
    val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
    val date = sdf.format(Date(event.receivedAt))

    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(event.sender, fontWeight = FontWeight.Bold)
                Text(date, fontSize = 12.sp, color = Color.Gray)
            }
            Text(event.body, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (logs.isEmpty()) {
                    Text("No matching rules", fontSize = 12.sp, color = Color.Gray)
                } else {
                    Column {
                        Text("Forwarding Logs:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        logs.forEach { log ->
                            Row(modifier = Modifier.fillMaxWidth(0.7f), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(log.status, color = if (log.status == "SUCCESS") Color.Green else Color.Red, fontSize = 12.sp)
                                Text(if (log.error != null) "Error: ${log.error}" else "OK", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                TextButton(onClick = onTestClick) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Test",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test", fontSize = 12.sp)
                }
            }
        }
    }
}
