package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandemailservice.entities.EmailSubscriptionEntity
import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import java.util.UUID

class EmailSubscriptionManagerTest {
    private lateinit var emailSender: EmailSender
    private lateinit var internalEmailBuilder: InternalEmailBuilder
    private lateinit var emailSubscriptionManager: EmailSubscriptionManager
    private lateinit var emailSubscriptionRepository: EmailSubscriptionRepository

    private val validUuid = UUID.randomUUID()
    private val emailAddress = "test@example.com"
    private val invalidUuid = UUID.randomUUID()

    private val validEmailSubscriptionEntity =
        EmailSubscriptionEntity(validUuid, emailAddress, true)

    @BeforeEach
    fun setup() {
        emailSender = mock(EmailSender::class.java)
        internalEmailBuilder = mock(InternalEmailBuilder::class.java)
        emailSubscriptionRepository = mock(EmailSubscriptionRepository::class.java)

        emailSubscriptionManager =
            EmailSubscriptionManager(
                emailSubscriptionRepository,
                emailSender,
                internalEmailBuilder,
            )

        `when`(emailSubscriptionRepository.findByUuid(validUuid)).thenReturn(validEmailSubscriptionEntity)
        `when`(emailSubscriptionRepository.findByUuid(invalidUuid)).thenReturn(null)
        `when`(internalEmailBuilder.buildInternalEmail(any())).thenReturn(mock())
        doNothing().whenever(emailSender).filterReceiversAndSendEmail(any())
    }

    @Test
    fun `validate that an email to the stakeholders is sent when someone unsubscribes an existing subscription`() {
        val response: ResponseEntity<String> = emailSubscriptionManager.unsubscribeUuidAndInformStakeholders(validUuid)

        assertEquals("Successfully unsubscribed email address corresponding to UUID: $validUuid.", response.body)
        verify(emailSender, times(1)).filterReceiversAndSendEmail(any())
    }

    @Test
    fun `validate that no email is sent to the stakeholders if there is no existing email subscription found`() {
        val response: ResponseEntity<String> =
            emailSubscriptionManager.unsubscribeUuidAndInformStakeholders(invalidUuid)

        assertEquals("There is no email address corresponding to UUID: $invalidUuid.", response.body)
        verify(emailSender, times(0)).filterReceiversAndSendEmail(any())
    }
}
