package personal.smsforwarder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import personal.smsforwarder.data.db.entities.ForwardingRule
import personal.smsforwarder.data.db.entities.Replacement
import personal.smsforwarder.engine.RuleEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegexTesterScreen(
    initialSender: String = "",
    initialBody: String = "",
    rules: List<ForwardingRule> = emptyList()
) {
    var sender by remember { mutableStateOf(initialSender) }
    var body by remember { mutableStateOf(initialBody) }
    var senderRegex by remember { mutableStateOf("") }
    var bodyRegex by remember { mutableStateOf("") }
    var replaceRegex by remember { mutableStateOf("") }
    var replaceWith by remember { mutableStateOf("") }

    // Rule selector state
    var selectedRule by remember { mutableStateOf<ForwardingRule?>(null) }
    var ruleDropdownExpanded by remember { mutableStateOf(false) }

    // Parse replacements from selected rule
    val ruleReplacements = remember(selectedRule) {
        selectedRule?.let { RuleEngine.parseReplacements(it.replacementsJson) } ?: emptyList()
    }

    val matches = remember(sender, body, senderRegex, bodyRegex) {
        if (senderRegex.isEmpty() && bodyRegex.isEmpty()) false
        else {
            try {
                val mockRule = ForwardingRule(
                    name = "Test",
                    senderRegex = if (senderRegex.isEmpty()) null else senderRegex,
                    bodyRegex = if (bodyRegex.isEmpty()) null else bodyRegex,
                    targetsJson = "[]"
                )
                RuleEngine.matches(mockRule, sender, body)
            } catch (e: Exception) {
                false
            }
        }
    }

    // Transform body using selected rule (with multiple replacements) or manual entry
    val transformedBody = remember(body, replaceRegex, replaceWith, selectedRule?.id, selectedRule?.replacementsJson) {
        try {
            if (selectedRule != null && selectedRule!!.replacementsJson != null) {
                // Use the full rule with all replacements
                RuleEngine.transformBody(selectedRule!!, body)
            } else if (selectedRule != null) {
                // Selected rule but no multiple replacements - use legacy single replacement
                val mockRule = ForwardingRule(
                    name = "Test",
                    replaceRegex = selectedRule!!.replaceRegex,
                    replaceWith = selectedRule!!.replaceWith,
                    targetsJson = "[]"
                )
                RuleEngine.transformBody(mockRule, body)
            } else if (replaceRegex.isNotEmpty()) {
                // Manual entry - single replacement
                val mockRule = ForwardingRule(
                    name = "Test",
                    replaceRegex = replaceRegex,
                    replaceWith = replaceWith,
                    targetsJson = "[]"
                )
                RuleEngine.transformBody(mockRule, body)
            } else {
                body
            }
        } catch (e: Exception) {
            body
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        Text("Regex Tester", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = sender, onValueChange = { sender = it }, label = { Text("Test Sender") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Test Body") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Rules to Test", style = MaterialTheme.typography.titleMedium)

        // Rule selector dropdown
        if (rules.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = ruleDropdownExpanded,
                onExpandedChange = { ruleDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedRule?.name ?: "Select existing rule...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Use Existing Rule") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ruleDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = ruleDropdownExpanded,
                    onDismissRequest = { ruleDropdownExpanded = false }
                ) {
                    // Option to clear selection
                    DropdownMenuItem(
                        text = { Text("-- Manual Entry --", color = Color.Gray) },
                        onClick = {
                            selectedRule = null
                            senderRegex = ""
                            bodyRegex = ""
                            replaceRegex = ""
                            replaceWith = ""
                            ruleDropdownExpanded = false
                        }
                    )
                    rules.forEach { rule ->
                        val replacementCount = RuleEngine.parseReplacements(rule.replacementsJson).size
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(rule.name, fontWeight = FontWeight.Medium)
                                    if (rule.bodyRegex != null) {
                                        Text(
                                            "Body: ${rule.bodyRegex}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    if (replacementCount > 0) {
                                        Text(
                                            "$replacementCount replacement(s)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedRule = rule
                                senderRegex = rule.senderRegex ?: ""
                                bodyRegex = rule.bodyRegex ?: ""
                                replaceRegex = rule.replaceRegex ?: ""
                                replaceWith = rule.replaceWith ?: ""
                                ruleDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(value = senderRegex, onValueChange = { senderRegex = it; selectedRule = null }, label = { Text("Sender Regex") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = bodyRegex, onValueChange = { bodyRegex = it; selectedRule = null }, label = { Text("Body Regex") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))
        Text("Matching Result:", fontWeight = FontWeight.SemiBold)
        Text(
            if (matches) "MATCH FOUND" else "NO MATCH",
            color = if (matches) Color.Green else Color.Red,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Replacement Test", style = MaterialTheme.typography.titleMedium)

        // Show multiple replacements from selected rule
        if (ruleReplacements.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Replacements from rule:", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            ruleReplacements.forEach { rep ->
                ReplacementItem(replacement = rep)
            }
        } else {
            // Manual entry fields
            OutlinedTextField(value = replaceRegex, onValueChange = { replaceRegex = it; selectedRule = null }, label = { Text("Replace Regex") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = replaceWith, onValueChange = { replaceWith = it; selectedRule = null }, label = { Text("Replace With") }, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Transformed Body:", fontWeight = FontWeight.SemiBold)
        Card(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(transformedBody, modifier = Modifier.padding(12.dp))
        }
    }
}

@Composable
fun ReplacementItem(replacement: Replacement) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = replacement.pattern,
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "to",
                modifier = Modifier.size(16.dp).padding(horizontal = 4.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = replacement.replacement.ifEmpty { "(empty)" },
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
