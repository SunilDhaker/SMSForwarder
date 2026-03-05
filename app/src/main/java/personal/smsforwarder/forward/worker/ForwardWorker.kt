package personal.smsforwarder.forward.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.serialization.json.Json
import personal.smsforwarder.data.db.AppDatabase
import personal.smsforwarder.data.db.entities.ForwardLog
import personal.smsforwarder.data.repo.LogRepository
import personal.smsforwarder.data.repo.RuleRepository
import personal.smsforwarder.engine.RuleEngine
import personal.smsforwarder.forward.ForwardDispatcher
import personal.smsforwarder.forward.ForwardTarget
import personal.smsforwarder.service.NotificationStats
import java.net.URI

class ForwardWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val db = AppDatabase.getDatabase(appContext)
    private val ruleRepo = RuleRepository(db.ruleDao())
    private val logRepo = LogRepository(db.smsEventDao(), db.forwardLogDao())
    private val dispatcher = ForwardDispatcher(appContext)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        val smsEventId = inputData.getLong("smsEventId", -1)
        if (smsEventId == -1L) return Result.failure()

        val event = logRepo.getEventById(smsEventId) ?: return Result.failure()
        val rules = ruleRepo.getEnabledRules()

        for (rule in rules) {
            if (RuleEngine.matches(rule, event.sender, event.body)) {
                val transformedBody = RuleEngine.transformBody(rule, event.body)
                val targets = try {
                    json.decodeFromString<List<ForwardTarget>>(rule.targetsJson)
                } catch (e: Exception) {
                    emptyList()
                }

                for (target in targets) {
                    val result = dispatcher.dispatch(target, rule, event, transformedBody)
                    val success = result.isSuccess

                    logRepo.insertLog(
                        ForwardLog(
                            smsEventId = smsEventId,
                            ruleId = rule.id,
                            target = targetSummary(target),
                            status = if (success) "SUCCESS" else "FAIL",
                            error = result.exceptionOrNull()?.message
                        )
                    )

                    // Update notification stats
                    NotificationStats.recordForward(
                        context = applicationContext,
                        sender = event.sender,
                        ruleName = rule.name,
                        success = success
                    )
                }

                if (rule.stopAfterMatch) break
            }
        }

        return Result.success()
    }

    private fun targetSummary(target: ForwardTarget): String = when (target) {
        is ForwardTarget.Sms -> "sms:${maskPhoneNumber(target.phoneNumber)}"
        is ForwardTarget.Webhook -> "webhook:${sanitizeWebhookUrl(target.url)}"
    }

    private fun maskPhoneNumber(phoneNumber: String): String {
        val digits = phoneNumber.filter { it.isDigit() }
        if (digits.isEmpty()) return "redacted"
        if (digits.length <= 4) return digits
        return "${"*".repeat(digits.length - 4)}${digits.takeLast(4)}"
    }

    private fun sanitizeWebhookUrl(rawUrl: String): String {
        return runCatching {
            val uri = URI(rawUrl)
            val host = uri.host ?: return@runCatching "redacted"
            val scheme = uri.scheme ?: "https"
            "$scheme://$host"
        }.getOrDefault("redacted")
    }
}
