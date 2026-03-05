package personal.smsforwarder.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import personal.smsforwarder.data.db.entities.SmsEvent

@Dao
interface SmsEventDao {
    @Query("SELECT * FROM sms_events ORDER BY receivedAt DESC")
    fun getAllEvents(): Flow<List<SmsEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SmsEvent): Long

    @Query("SELECT * FROM sms_events WHERE id = :id")
    suspend fun getEventById(id: Long): SmsEvent?

    @Query("SELECT * FROM sms_events WHERE messageHash = :hash AND receivedAt > :since")
    suspend fun findRecentByHash(hash: String, since: Long): SmsEvent?
}
