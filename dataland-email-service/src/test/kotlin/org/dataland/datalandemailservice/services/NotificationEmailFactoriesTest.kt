package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.services.templateemail.SingleNotificationEmailFactory
import org.dataland.datalandemailservice.services.templateemail.SummaryNotificationEmailFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID

class NotificationEmailFactoriesTest {
    private val proxyPrimaryUrl = "local-dev.dataland.com"
    private val senderEmail = "sender@dataland.com"
    private val senderName = "Dataland"
    private val receiverEmail = "user@testemail.com"

    private val dummyCompanyName = "some-company-name"
    private val dummyCompanyId = "some-id"

    private val mockUuid = UUID.randomUUID()
    private val mockUuidString = mockUuid.toString()

    private val emailSubscriptionTrackerMock = Mockito.mock(EmailSubscriptionTracker::class.java)

    @BeforeEach
    fun setup() {
        Mockito
            .`when`(emailSubscriptionTrackerMock.addSubscriptionIfNeededAndReturnUuid(receiverEmail))
            .thenReturn(mockUuid)
    }

    @Test
    fun `validate the text of the notification mail for one single data upload`() {
        val singleNotificationEmailFactory =
            SingleNotificationEmailFactory(proxyPrimaryUrl, senderEmail, senderName, emailSubscriptionTrackerMock)

        val someFramework = "some-framework"
        val someYear = "2019"
        val properties =
            mapOf(
                "companyName" to dummyCompanyName,
                "companyId" to dummyCompanyId,
                "framework" to someFramework,
                "year" to someYear,
                "baseUrl" to proxyPrimaryUrl,
                "subscriptionUuid" to mockUuidString,
            )

        val mail = singleNotificationEmailFactory.buildEmail(receiverEmail, properties)
        assertEquals(
            "New data for $dummyCompanyName on Dataland",
            mail.content.subject,
        )
        assertTrue(mail.content.textContent.contains(dummyCompanyName))
        assertTrue(mail.content.textContent.contains(dummyCompanyId))
        assertTrue(mail.content.textContent.contains(someFramework))
        assertTrue(mail.content.textContent.contains(someYear))
        assertTrue(mail.content.textContent.contains(proxyPrimaryUrl))
    }

    @Test
    fun `validate the text of the summary notification mail for several data uploads`() {
        val summaryNotificationEmailFactory =
            SummaryNotificationEmailFactory(proxyPrimaryUrl, senderEmail, senderName, emailSubscriptionTrackerMock)

        val frameworks = "framework-alpha, framework-beta"
        val numberOfDays = "12"
        val properties =
            mutableMapOf(
                "companyName" to dummyCompanyName,
                "companyId" to dummyCompanyId,
                "frameworks" to frameworks,
                "baseUrl" to proxyPrimaryUrl,
                "numberOfDays" to numberOfDays,
                "subscriptionUuid" to mockUuidString,
            )

        val mail = summaryNotificationEmailFactory.buildEmail(receiverEmail, properties)
        assertEquals(
            "New data for $dummyCompanyName on Dataland",
            mail.content.subject,
        )
        assertTrue(mail.content.textContent.contains(dummyCompanyName))
        assertTrue(mail.content.textContent.contains(dummyCompanyId))
        assertTrue(mail.content.textContent.contains(frameworks))
        assertTrue(mail.content.textContent.contains("$numberOfDays days"))
        assertTrue(mail.content.textContent.contains(proxyPrimaryUrl))

        properties["numberOfDays"] = "0"
        assertTrue(
            summaryNotificationEmailFactory.buildEmail(receiverEmail, properties).content.textContent.contains(
                "24 hours",
            ),
        )
    }
}
