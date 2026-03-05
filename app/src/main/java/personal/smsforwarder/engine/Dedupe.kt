package personal.smsforwarder.engine

import java.security.MessageDigest

object Dedupe {
    fun calculateHash(sender: String, body: String, timestamp: Long): String {
        // Use a 1-minute window for timestamp-based dedupe
        val timeBucket = timestamp / 60_000
        val input = "$sender|$timeBucket|$body"
        return sha256(input)
    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}
