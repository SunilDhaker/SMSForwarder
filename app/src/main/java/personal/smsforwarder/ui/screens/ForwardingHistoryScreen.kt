package personal.smsforwarder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.navigation.NavController
import java.net.URLEncoder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import personal.smsforwarder.data.db.entities.ForwardLog
import personal.smsforwarder.data.db.entities.ForwardingRule
import personal.smsforwarder.data.db.entities.SmsEvent
import personal.smsforwarder.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForwardingHistoryScreen(viewModel: AppViewModel, navController: NavController) {
    val events by viewModel.allEvents.collectAsState()
    val logs by viewModel.allLogs.collectAsState()
    val rules by viewModel.allRules.collectAsState()
    val logsByEventId = remember(logs) { logs.groupBy { it.smsEventId } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forwarding History") }
            )
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No forwarding history yet", fontSize = 18.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Messages will appear here once forwarded", fontSize = 14.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                items(events.sortedByDescending { it.receivedAt }) { event ->
                    val eventLogs = logsByEventId[event.id].orEmpty()
                    ForwardingHistoryItem(
                        event = event,
                        logs = eventLogs,
                        rules = rules,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForwardingHistoryItem(
    event: SmsEvent,
    logs: List<ForwardLog>,
    rules: List<ForwardingRule>,
    onTestClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    val date = sdf.format(Date(event.receivedAt))

    val hasSuccess = logs.any { it.status == "SUCCESS" }
    val hasFailed = logs.any { it.status == "FAIL" }
    val statusColor = when {
        hasSuccess && !hasFailed -> Color(0xFF4CAF50) // Green
        hasFailed && !hasSuccess -> Color(0xFFF44336) // Red
        hasSuccess && hasFailed -> Color(0xFFFF9800) // Orange (partial)
        else -> Color.Gray // No logs
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row with status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = event.sender,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = date,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message preview
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = event.body.take(100) + if (event.body.length > 100) "..." else "",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }

            // Forwarding summary and Test button
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (logs.isNotEmpty()) {
                    Column {
                        Text(
                            text = "${logs.size} forwarding attempt(s)",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${logs.count { it.status == "SUCCESS" }} success, ${logs.count { it.status == "FAIL" }} failed",
                            fontSize = 12.sp,
                            color = statusColor
                        )
                    }
                } else {
                    Text(
                        text = "No matching rules",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
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

            // Expanded details
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Full message
                Text(
                    text = "Full Message",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 1.dp
                ) {
                    Text(
                        text = event.body,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }

                // Forwarding logs
                if (logs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Forwarding Details",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    logs.forEach { log ->
                        val matchingRule = rules.find { it.id == log.ruleId }
                        ForwardingLogDetail(log = log, ruleName = matchingRule?.name)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ForwardingLogDetail(
    log: ForwardLog,
    ruleName: String?
) {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val time = sdf.format(Date(log.createdAt))
    val isSuccess = log.status == "SUCCESS"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isSuccess)
            Color(0xFF4CAF50).copy(alpha = 0.1f)
        else
            Color(0xFFF44336).copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                contentDescription = log.status,
                tint = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.target,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    if (ruleName != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = ruleName,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                if (log.error != null) {
                    Text(
                        text = log.error,
                        fontSize = 12.sp,
                        color = Color(0xFFF44336)
                    )
                }
            }
            Text(
                text = time,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
