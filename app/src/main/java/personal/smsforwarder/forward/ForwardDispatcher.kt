package personal.smsforwarder.forward

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import personal.smsforwarder.data.db.entities.ForwardingRule
import personal.smsforwarder.data.db.entities.SmsEvent

@Serializable
sealed class ForwardTarget {
    @Serializable
    data class Sms(val phoneNumber: String, val subscriptionId: Int? = null) : ForwardTarget()
    
    @Serializable
    data class Webhook(val url: String, val secretToken: String? = null) : ForwardTarget()
}

class ForwardDispatcher(private val context: Context) {
    private val httpClient = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun dispatch(target: ForwardTarget, rule: ForwardingRule, event: SmsEvent, transformedBody: String): Result<Unit> {
        return when (target) {
            is ForwardTarget.Sms -> sendSms(target, transformedBody)
            is ForwardTarget.Webhook -> sendWebhook(target, rule, event, transformedBody)
        }
    }

    private fun sendSms(target: ForwardTarget.Sms, body: String): Result<Unit> {
        return try {
            val smsManager: SmsManager = if (
                target.subscriptionId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            ) {
                context.getSystemService(SmsManager::class.java).createForSubscriptionId(target.subscriptionId)
            } else {
                context.getSystemService(SmsManager::class.java)
            }
            
            val prefixedBody = "[FW] $body"
            val parts = smsManager.divideMessage(prefixedBody)
            smsManager.sendMultipartTextMessage(target.phoneNumber, null, parts, null, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun sendWebhook(target: ForwardTarget.Webhook, rule: ForwardingRule, event: SmsEvent, transformedBody: String): Result<Unit> {
        return try {
            val payload = mapOf(
                "receivedAt" to event.receivedAt,
                "sender" to event.sender,
                "originalBody" to event.body,
                "transformedBody" to transformedBody,
                "ruleName" to rule.name,
                "idempotencyKey" to event.messageHash
            )
            
            val body = json.encodeToString(payload).toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(target.url)
                .post(body)
                .apply {
                    target.secretToken?.let { header("X-Forwarder-Token", it) }
                }
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
