package personal.smsforwarder.data.repo

import kotlinx.coroutines.flow.Flow
import personal.smsforwarder.data.db.dao.SmsEventDao
import personal.smsforwarder.data.db.dao.ForwardLogDao
import personal.smsforwarder.data.db.entities.SmsEvent
import personal.smsforwarder.data.db.entities.ForwardLog

class LogRepository(
    private val smsEventDao: SmsEventDao,
    private val forwardLogDao: ForwardLogDao
) {
    val allEvents: Flow<List<SmsEvent>> = smsEventDao.getAllEvents()
    val allLogs: Flow<List<ForwardLog>> = forwardLogDao.getAllLogs()

    suspend fun insertEvent(event: SmsEvent): Long = smsEventDao.insertEvent(event)

    suspend fun getEventById(id: Long) = smsEventDao.getEventById(id)

    suspend fun findRecentByHash(hash: String, since: Long) = smsEventDao.findRecentByHash(hash, since)

    suspend fun insertLog(log: ForwardLog) = forwardLogDao.insertLog(log)

    suspend fun getLogsForSmsEvent(smsEventId: Long) = forwardLogDao.getLogsForSmsEvent(smsEventId)
}
