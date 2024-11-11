package org.dataland.datalandemailservice.email

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.dataland.datalandemailservice.services.EmailSubscriptionTracker
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import java.io.File
import java.util.UUID

typealias TestData = TypedEmailContentTestData

class TypedEmailContentTest {
    @Test
    fun `test that every template has test data`() {
        TypedEmailContent::class.sealedSubclasses.forEach { subclass ->
            assertTrue(TestData.contentToKeywordsMap.any { (typedEmailContent, _) -> subclass.isInstance(typedEmailContent) })
        }
    }

    @Test
    fun `test that every template can be constructed without exception and contains all keywords`() {
        TestData.contentToKeywordsMap.forEach { (typedEmailContent, keywords) ->

            assertDoesNotThrow {
                val emailContent = typedEmailContent.build()
                keywords.forEach { keyword ->
                    assertThat(emailContent.htmlContent).contains(keyword)
                    assertThat(emailContent.textContent).contains(keyword)
                }
                saveEmailContent(typedEmailContent::class.simpleName ?: UUID.randomUUID().toString(), emailContent)
            }
        }
    }

    private fun saveEmailContent(
        name: String,
        emailContent: EmailContent,
    ) {
        val tmpDir = File("tmp")
        if (!tmpDir.exists()) tmpDir.mkdirs()

        val txtFile = File(tmpDir, "$name.txt")
        val htmlFile = File(tmpDir, "$name.html")

        txtFile.writeText(emailContent.textContent)
        htmlFile.writeText(emailContent.htmlContent)

        println("Saved email as ${txtFile.absolutePath} and ${htmlFile.absolutePath}")
    }

    @Test
    fun `test that late init vars specified in interfaces are correctly injected`() {
        val emailSubscriptionTracker = mock<EmailSubscriptionTracker>()
        val content = TestData.singleDatasetUploadedEngagement
        val subscriptionUuid = UUID.randomUUID()
        val receiver =
            mapOf(
                EmailContact("test3@example.com") to subscriptionUuid,
            )
        val proxyPrimaryUrl = "abc.dataland.com"

        content.setLateInitVars(receiver, proxyPrimaryUrl, emailSubscriptionTracker)

        assertEquals(content.baseUrl, proxyPrimaryUrl)
        assertEquals(content.subscriptionUuid, subscriptionUuid.toString())
    }

    @Test
    fun `test that late init subscription statuses are correctly injected`() {
        val subscribedEmail = "testA@example.com"
        val unsubscribedEmail = "testB@example.com"

        val subscribedValue = Value.EmailAddressWithSubscriptionStatus(subscribedEmail)
        val unsubscribedValue = Value.EmailAddressWithSubscriptionStatus(unsubscribedEmail)

        val emailSubscriptionTracker = mock<EmailSubscriptionTracker>()
        `when`(emailSubscriptionTracker.shouldReceiveEmail(subscribedEmail)).thenReturn(true)
        `when`(emailSubscriptionTracker.shouldReceiveEmail(unsubscribedEmail)).thenReturn(false)

        subscribedValue.setLateInitVars(emailSubscriptionTracker)
        unsubscribedValue.setLateInitVars(emailSubscriptionTracker)

        assertTrue(subscribedValue.subscribed)
        assertFalse(unsubscribedValue.subscribed)
    }
}
