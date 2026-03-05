package personal.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import personal.smsforwarder.data.db.AppDatabase
import personal.smsforwarder.data.db.entities.SmsEvent
import personal.smsforwarder.data.repo.LogRepository
import personal.smsforwarder.engine.Dedupe

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (messages.isNullOrEmpty()) return@launch

                val sender = messages.first().originatingAddress.orEmpty()
                val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }

                // Loop prevention: ignore messages forwarded by us
                if (body.startsWith("[FW] ")) return@launch

                val timestamp = System.currentTimeMillis()
                val hash = Dedupe.calculateHash(sender, body, timestamp)

                val db = AppDatabase.getDatabase(context)
                val logRepo = LogRepository(db.smsEventDao(), db.forwardLogDao())

                // Dedupe check: check if this hash was seen in the last 2 minutes
                val recent = logRepo.findRecentByHash(hash, timestamp - 120_000)
                if (recent != null) return@launch

                val smsEventId = logRepo.insertEvent(
                    SmsEvent(
                        receivedAt = timestamp,
                        sender = sender,
                        body = body,
                        messageHash = hash
                    )
                )

                // Enqueue ForwardWorker
                enqueueForwardWorker(context, smsEventId)

            } finally {
                result.finish()
            }
        }
    }

    private fun enqueueForwardWorker(context: Context, smsEventId: Long) {
        val work = OneTimeWorkRequestBuilder<personal.smsforwarder.forward.worker.ForwardWorker>()
            .setInputData(workDataOf("smsEventId" to smsEventId))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        // Use unique work to prevent duplicate processing
        WorkManager.getInstance(context).enqueueUniqueWork(
            "forward_sms_$smsEventId",
            ExistingWorkPolicy.KEEP,
            work
        )
    }
}
