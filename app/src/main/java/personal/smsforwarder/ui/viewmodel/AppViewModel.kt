package personal.smsforwarder.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import personal.smsforwarder.data.db.AppDatabase
import personal.smsforwarder.data.db.entities.ForwardLog
import personal.smsforwarder.data.db.entities.ForwardingRule
import personal.smsforwarder.data.db.entities.SmsEvent
import personal.smsforwarder.data.repo.LogRepository
import personal.smsforwarder.data.repo.RuleRepository

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val ruleRepo = RuleRepository(db.ruleDao())
    private val logRepo = LogRepository(db.smsEventDao(), db.forwardLogDao())

    val allRules: StateFlow<List<ForwardingRule>> = ruleRepo.allRules.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allEvents: StateFlow<List<SmsEvent>> = logRepo.allEvents.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allLogs: StateFlow<List<ForwardLog>> = logRepo.allLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun saveRule(rule: ForwardingRule) {
        viewModelScope.launch {
            if (rule.id == 0L) {
                ruleRepo.insert(rule)
            } else {
                ruleRepo.update(rule)
            }
        }
    }

    fun deleteRule(rule: ForwardingRule) {
        viewModelScope.launch {
            ruleRepo.delete(rule)
        }
    }

    fun toggleRule(rule: ForwardingRule) {
        viewModelScope.launch {
            ruleRepo.update(rule.copy(enabled = !rule.enabled))
        }
    }
}
