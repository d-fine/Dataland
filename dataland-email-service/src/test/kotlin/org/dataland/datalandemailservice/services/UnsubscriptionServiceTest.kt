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

class UnsubscriptionServiceTest {
    private lateinit var emailSender: EmailSender
    private lateinit var internalEmailBuilder: InternalEmailBuilder
    private lateinit var emailSubscriptionService: EmailUnsubscriber
    private lateinit var emailSubscriptionRepository: EmailSubscriptionRepository
    private lateinit var unsubscriptionService: UnsubscriptionService

    private val validUuid = UUID.randomUUID()
    private val emailAddress = "test@example.com"
    private val invalidUuid = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        emailSender = mock(EmailSender::class.java)
        internalEmailBuilder = mock(InternalEmailBuilder::class.java)
        emailSubscriptionService = mock(EmailUnsubscriber::class.java)
        emailSubscriptionRepository = mock(EmailSubscriptionRepository::class.java)

        unsubscriptionService =
            UnsubscriptionService(
                emailSender,
                internalEmailBuilder,
                emailSubscriptionService,
                emailSubscriptionRepository,
            )
    }

    @Test
    fun `should unsubscribe email and send notification when email exists`() {
        val validEmailSubscriptionEntity =
            EmailSubscriptionEntity(validUuid, emailAddress, true) // Assuming you have this class.
        `when`(emailSubscriptionRepository.findByUuid(validUuid)).thenReturn(validEmailSubscriptionEntity)
        `when`(emailSubscriptionRepository.findByUuid(invalidUuid)).thenReturn(null)
        `when`(internalEmailBuilder.buildInternalEmail(any())).thenReturn(mock())
        doNothing().whenever(emailSubscriptionService).unsubscribeEmailWithUuid(validUuid)
        doNothing().whenever(emailSubscriptionService).unsubscribeEmailWithUuid(invalidUuid)
        doNothing().whenever(emailSender).filterReceiversAndSentEmail(any())

        val response: ResponseEntity<String> = unsubscriptionService.unsubscribeUuidAndSendMailToStakeholders(validUuid)

        assertEquals("Successfully unsubscribed email address corresponding to the UUID: $validUuid.", response.body)
        verify(emailSubscriptionService, times(1)).unsubscribeEmailWithUuid(validUuid)
        verify(emailSender, times(1)).filterReceiversAndSentEmail(any())
    }

    @Test
    fun `should return message when no email subscription found`() {
        `when`(emailSubscriptionRepository.findByUuid(invalidUuid)).thenReturn(null)

        val response: ResponseEntity<String> =
            unsubscriptionService.unsubscribeUuidAndSendMailToStakeholders(invalidUuid)

        assertEquals("There is no email address corresponding to the UUID: $invalidUuid.", response.body)
        verify(emailSubscriptionService, times(0)).unsubscribeEmailWithUuid(any())
        verify(emailSender, times(0)).filterReceiversAndSentEmail(any())
    }
}
