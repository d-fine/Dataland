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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import java.util.UUID

class TypedEmailContentTest {
    @ParameterizedTest
    @ArgumentsSource(TypedEmailContentTestData::class)
    fun `test that every template has test data`(typedEmailContent: TypedEmailContent) {
        TypedEmailContent::class
            .sealedSubclasses
            .any { subclass ->
                subclass.isInstance(typedEmailContent)
            }.let(::assertTrue)
    }

    private val htmlTagPattern = Regex("<[^>]+>")

    @ParameterizedTest
    @ArgumentsSource(TypedEmailContentTestData::class)
    fun `test that every template can be constructed without exception and contains all keywords and the text email has no html tags`(
        typedEmailContent: TypedEmailContent,
        keywords: List<String>,
    ) {
        assertDoesNotThrow {
            val emailContent = typedEmailContent.build()
            keywords.forEach { keyword ->
                assertThat(emailContent.htmlContent).contains(keyword)
                assertThat(emailContent.textContent).contains(keyword)
            }
            assertFalse(htmlTagPattern.containsMatchIn(emailContent.textContent))
        }
    }

    @Test
    fun `test that late init vars specified in interfaces are correctly injected`() {
        val emailSubscriptionTracker = mock<EmailSubscriptionTracker>()
        val content = TypedEmailContentTestData().accessToDatasetRequestedEmailContent
        val subscriptionUuid = UUID.randomUUID()
        val receiver =
            mapOf(
                EmailContact("test3@example.com") to subscriptionUuid,
            )
        val proxyPrimaryUrl = "abc.dataland.com"

        content.setLateInitVars(receiver, proxyPrimaryUrl, emailSubscriptionTracker)

        assertEquals(content.baseUrl, "https://$proxyPrimaryUrl")
    }

    @Test
    fun `test that late init subscription statuses are correctly injected`() {
        val subscribedContact = EmailContact.create("testA@example.com")
        val unsubscribedContact = EmailContact.create("testB@example.com")

        val subscribedValue = Value.EmailAddressWithSubscriptionStatus(subscribedContact.emailAddress)
        val unsubscribedValue = Value.EmailAddressWithSubscriptionStatus(unsubscribedContact.emailAddress)

        val emailSubscriptionTracker = mock<EmailSubscriptionTracker>()
        `when`(emailSubscriptionTracker.shouldReceiveEmail(subscribedContact)).thenReturn(true)
        `when`(emailSubscriptionTracker.shouldReceiveEmail(unsubscribedContact)).thenReturn(false)

        subscribedValue.setLateInitVars(emailSubscriptionTracker)
        unsubscribedValue.setLateInitVars(emailSubscriptionTracker)

        assertTrue(subscribedValue.subscribed)
        assertFalse(unsubscribedValue.subscribed)
    }
}
