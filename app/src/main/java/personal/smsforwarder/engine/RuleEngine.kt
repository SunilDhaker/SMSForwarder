package personal.smsforwarder.engine

import kotlinx.serialization.json.Json
import personal.smsforwarder.data.db.entities.ForwardingRule
import personal.smsforwarder.data.db.entities.Replacement

object RuleEngine {

    private val json = Json { ignoreUnknownKeys = true }

    fun matches(rule: ForwardingRule, sender: String, body: String): Boolean {
        if (!rule.enabled) return false

        val senderMatches = rule.senderRegex?.let { regex ->
            Regex(regex, RegexOption.IGNORE_CASE).containsMatchIn(sender)
        } ?: true

        val bodyMatches = rule.bodyRegex?.let { regex ->
            Regex(regex, RegexOption.IGNORE_CASE).containsMatchIn(body)
        } ?: true

        return senderMatches && bodyMatches
    }

    fun transformBody(rule: ForwardingRule, body: String): String {
        var result = body

        // Apply single replacement (legacy)
        if (rule.replaceRegex != null) {
            val replacement = rule.replaceWith ?: ""
            try {
                result = Regex(rule.replaceRegex, RegexOption.IGNORE_CASE).replace(result, replacement)
            } catch (e: Exception) {
                // Ignore regex errors
            }
        }

        // Apply multiple replacements
        if (rule.replacementsJson != null) {
            try {
                val replacements = json.decodeFromString<List<Replacement>>(rule.replacementsJson)
                for (rep in replacements) {
                    try {
                        result = Regex(rep.pattern, RegexOption.IGNORE_CASE).replace(result, rep.replacement)
                    } catch (e: Exception) {
                        // Ignore individual replacement errors
                    }
                }
            } catch (e: Exception) {
                // Ignore JSON parsing errors
            }
        }

        return result
    }

    fun parseReplacements(replacementsJson: String?): List<Replacement> {
        if (replacementsJson == null) return emptyList()
        return try {
            json.decodeFromString(replacementsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeReplacements(replacements: List<Replacement>): String {
        return json.encodeToString(kotlinx.serialization.builtins.ListSerializer(Replacement.serializer()), replacements)
    }
}
