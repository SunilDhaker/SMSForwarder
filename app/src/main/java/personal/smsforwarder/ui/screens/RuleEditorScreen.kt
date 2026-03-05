package personal.smsforwarder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import personal.smsforwarder.data.db.entities.ForwardingRule
import personal.smsforwarder.forward.ForwardTarget
import personal.smsforwarder.ui.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditorScreen(navController: NavController, viewModel: AppViewModel, ruleId: Long) {
    val rules by viewModel.allRules.collectAsState()
    val existingRule = rules.find { it.id == ruleId }

    var name by remember { mutableStateOf(existingRule?.name ?: "") }
    var senderRegex by remember { mutableStateOf(existingRule?.senderRegex ?: "") }
    var bodyRegex by remember { mutableStateOf(existingRule?.bodyRegex ?: "") }
    var replaceRegex by remember { mutableStateOf(existingRule?.replaceRegex ?: "") }
    var replaceWith by remember { mutableStateOf(existingRule?.replaceWith ?: "") }
    var stopAfterMatch by remember { mutableStateOf(existingRule?.stopAfterMatch ?: false) }
    
    // For simplicity in MVP, we handle one target of each type or a list
    // In a full app, we'd have a dynamic list of targets
    var smsTargetNumber by remember { mutableStateOf("") }
    var webhookUrl by remember { mutableStateOf("") }

    LaunchedEffect(existingRule) {
        existingRule?.let {
            val targets = try {
                Json.decodeFromString<List<ForwardTarget>>(it.targetsJson)
            } catch (e: Exception) {
                emptyList()
            }
            smsTargetNumber = targets.filterIsInstance<ForwardTarget.Sms>().firstOrNull()?.phoneNumber ?: ""
            webhookUrl = targets.filterIsInstance<ForwardTarget.Webhook>().firstOrNull()?.url ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (ruleId == 0L) "New Rule" else "Edit Rule") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Rule Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = senderRegex, onValueChange = { senderRegex = it }, label = { Text("Sender Regex (Optional)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = bodyRegex, onValueChange = { bodyRegex = it }, label = { Text("Body Regex (Optional)") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Replacement (Optional)", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = replaceRegex, onValueChange = { replaceRegex = it }, label = { Text("Replace Regex") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = replaceWith, onValueChange = { replaceWith = it }, label = { Text("Replace With") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
            Text("Targets", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = smsTargetNumber, onValueChange = { smsTargetNumber = it }, label = { Text("Forward to SMS Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = webhookUrl, onValueChange = { webhookUrl = it }, label = { Text("Forward to Webhook URL") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Stop after match")
                Switch(checked = stopAfterMatch, onCheckedChange = { stopAfterMatch = it })
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val targets = mutableListOf<ForwardTarget>()
                    if (smsTargetNumber.isNotBlank()) targets.add(ForwardTarget.Sms(smsTargetNumber))
                    if (webhookUrl.isNotBlank()) targets.add(ForwardTarget.Webhook(webhookUrl))

                    val rule = ForwardingRule(
                        id = ruleId,
                        name = name,
                        senderRegex = if (senderRegex.isBlank()) null else senderRegex,
                        bodyRegex = if (bodyRegex.isBlank()) null else bodyRegex,
                        replaceRegex = if (replaceRegex.isBlank()) null else replaceRegex,
                        replaceWith = if (replaceWith.isBlank()) null else replaceWith,
                        targetsJson = Json.encodeToString(targets),
                        stopAfterMatch = stopAfterMatch,
                        enabled = existingRule?.enabled ?: true
                    )
                    viewModel.saveRule(rule)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && (smsTargetNumber.isNotBlank() || webhookUrl.isNotBlank())
            ) {
                Text("Save Rule")
            }
        }
    }
}
