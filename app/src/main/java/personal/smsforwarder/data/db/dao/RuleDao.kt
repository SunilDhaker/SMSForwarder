package personal.smsforwarder.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import personal.smsforwarder.data.db.entities.ForwardingRule

@Dao
interface RuleDao {
    @Query("SELECT * FROM forwarding_rules ORDER BY priority ASC")
    fun getAllRules(): Flow<List<ForwardingRule>>

    @Query("SELECT * FROM forwarding_rules WHERE enabled = 1 ORDER BY priority ASC")
    suspend fun getEnabledRules(): List<ForwardingRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ForwardingRule)

    @Update
    suspend fun updateRule(rule: ForwardingRule)

    @Delete
    suspend fun deleteRule(rule: ForwardingRule)

    @Query("SELECT * FROM forwarding_rules WHERE id = :id")
    suspend fun getRuleById(id: Long): ForwardingRule?
}
