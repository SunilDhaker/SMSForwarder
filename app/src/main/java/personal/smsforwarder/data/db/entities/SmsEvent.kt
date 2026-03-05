package personal.smsforwarder.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_events",
    indices = [Index(value = ["messageHash", "receivedAt"])]
)
data class SmsEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receivedAt: Long,
    val sender: String,
    val body: String,
    val subscriptionId: Int? = null, // for dual SIM
    val messageHash: String // for dedupe
)
