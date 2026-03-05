package personal.smsforwarder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import personal.smsforwarder.data.db.entities.ForwardingRule
import personal.smsforwarder.ui.viewmodel.AppViewModel

@Composable
fun RuleListScreen(navController: NavController, viewModel: AppViewModel) {
    val rules by viewModel.allRules.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("rule_editor/0") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Rule")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (rules.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No rules configured. Tap + to add one.", color = Color.Gray)
                    }
                }
            } else {
                items(rules) { rule ->
                    RuleItem(
                        rule = rule,
                        onToggle = { viewModel.toggleRule(rule) },
                        onEdit = { navController.navigate("rule_editor/${rule.id}") },
                        onDelete = { viewModel.deleteRule(rule) }
                    )
                }
            }
        }
    }
}

@Composable
fun RuleItem(
    rule: ForwardingRule,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(rule.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        if (rule.enabled) "Enabled" else "Disabled",
                        color = if (rule.enabled) Color.Green else Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Switch(checked = rule.enabled, onCheckedChange = { onToggle() })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("From: ${rule.senderRegex ?: "Any"}", fontSize = 14.sp)
            Text("Body: ${rule.bodyRegex ?: "Any"}", fontSize = 14.sp)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}
