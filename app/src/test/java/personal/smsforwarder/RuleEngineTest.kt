package personal.smsforwarder

import org.junit.Test
import org.junit.Assert.*
import personal.smsforwarder.data.db.entities.ForwardingRule
import personal.smsforwarder.engine.RuleEngine
import personal.smsforwarder.engine.Dedupe

class RuleEngineTest {

    @Test
    fun testRuleMatching() {
        val rule = ForwardingRule(
            name = "Bank OTP",
            senderRegex = ".*BANK.*",
            bodyRegex = ".*OTP.*",
            targetsJson = "[]"
        )

        // Positive match
        assertTrue(RuleEngine.matches(rule, "AD-BANK", "Your OTP is 123456"))
        
        // Negative matches
        assertFalse(RuleEngine.matches(rule, "SHOP-NOTIFY", "Your OTP is 123456"))
        assertFalse(RuleEngine.matches(rule, "AD-BANK", "Your balance is 100"))
    }

    @Test
    fun testBodyTransformation() {
        val rule = ForwardingRule(
            name = "Extract OTP",
            replaceRegex = ".*OTP is (\\d{6}).*",
            replaceWith = "OTP: $1",
            targetsJson = "[]"
        )

        val originalBody = "Your bank OTP is 123456. Do not share."
        val transformedBody = RuleEngine.transformBody(rule, originalBody)
        
        assertEquals("OTP: 123456", transformedBody)
    }

    @Test
    fun testCaseInsensitiveMatching() {
        val rule = ForwardingRule(
            name = "Case Insensitive",
            senderRegex = "bank",
            targetsJson = "[]"
        )

        assertTrue(RuleEngine.matches(rule, "BANK", "any content"))
    }

    @Test
    fun testDedupeHashing() {
        val sender = "Sender"
        val body = "Body"
        val now = 1735560000000L // Some timestamp

        val hash1 = Dedupe.calculateHash(sender, body, now)
        val hash2 = Dedupe.calculateHash(sender, body, now + 30000) // Within same 1-min bucket
        val hash3 = Dedupe.calculateHash(sender, body, now + 70000) // Different bucket

        assertEquals(hash1, hash2)
        assertNotEquals(hash1, hash3)
    }
}
