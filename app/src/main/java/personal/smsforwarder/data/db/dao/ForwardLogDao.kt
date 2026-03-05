package personal.smsforwarder.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import personal.smsforwarder.data.db.entities.ForwardLog

@Dao
interface ForwardLogDao {
    @Query("SELECT * FROM forward_logs ORDER BY createdAt DESC")
    fun getAllLogs(): Flow<List<ForwardLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ForwardLog)

    @Query("SELECT * FROM forward_logs WHERE smsEventId = :smsEventId")
    suspend fun getLogsForSmsEvent(smsEventId: Long): List<ForwardLog>
}
