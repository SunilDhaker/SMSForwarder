package personal.smsforwarder.data.repo

import kotlinx.coroutines.flow.Flow
import personal.smsforwarder.data.db.dao.RuleDao
import personal.smsforwarder.data.db.entities.ForwardingRule

class RuleRepository(private val ruleDao: RuleDao) {
    val allRules: Flow<List<ForwardingRule>> = ruleDao.getAllRules()

    suspend fun getEnabledRules(): List<ForwardingRule> = ruleDao.getEnabledRules()

    suspend fun insert(rule: ForwardingRule) = ruleDao.insertRule(rule)

    suspend fun update(rule: ForwardingRule) = ruleDao.updateRule(rule)

    suspend fun delete(rule: ForwardingRule) = ruleDao.deleteRule(rule)

    suspend fun getRuleById(id: Long) = ruleDao.getRuleById(id)
}
