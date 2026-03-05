package personal.smsforwarder.data.db.entities

import androidx.room.*

@Entity(
    tableName = "forward_logs",
    foreignKeys = [
        ForeignKey(
            entity = SmsEvent::class,
            parentColumns = ["id"],
            childColumns = ["smsEventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ForwardingRule::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("smsEventId"),
        Index("ruleId")
    ]
)
data class ForwardLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val smsEventId: Long,
    val ruleId: Long?,
    val target: String,
    val status: String, // SUCCESS, FAIL, SKIP
    val error: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
