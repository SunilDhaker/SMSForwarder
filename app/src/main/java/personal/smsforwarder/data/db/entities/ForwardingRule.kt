package personal.smsforwarder.data.db.entities

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable
data class Replacement(
    val pattern: String,
    val replacement: String
)

@Entity(tableName = "forwarding_rules")
@Serializable
data class ForwardingRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val enabled: Boolean = true,
    val senderRegex: String? = null,
    val bodyRegex: String? = null,
    val replaceRegex: String? = null,
    val replaceWith: String? = null,
    val replacementsJson: String? = null, // JSON array of Replacement objects for multiple replacements
    val targetsJson: String, // Serialized list of targets
    val stopAfterMatch: Boolean = false,
    val dedupeWindowMs: Long = 60_000,
    val priority: Int = 0,
    val isPreAdded: Boolean = false
)
