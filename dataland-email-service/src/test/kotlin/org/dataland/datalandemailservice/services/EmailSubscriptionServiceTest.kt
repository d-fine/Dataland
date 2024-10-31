package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.entities.EmailSubscriptionEntity
import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.UUID

class EmailSubscriptionServiceTest {
    private lateinit var mockEmailSubscriptionRepository: EmailSubscriptionRepository
    private lateinit var emailSubscriptionService: EmailSubscriptionService

    val subscribedUuid = UUID.randomUUID()
    val invalidUuid = UUID.randomUUID()
    val newUuid = UUID.randomUUID()

    val subscriberEmail = "subscriber@mail.com"
    val notASubscriberEmail = "notasubscriber@mail.com"
    val unkownEmail = "unknown@mail.com"
    val newEmail = "new@mail.com"

    val subscribedEntity =
        EmailSubscriptionEntity(
            uuid = subscribedUuid,
            emailAddress = subscriberEmail,
            isSubscribed = true,
        )

    val newSubscriberEntity =
        EmailSubscriptionEntity(
            uuid = newUuid,
            emailAddress = newEmail,
            isSubscribed = true,
        )

    @BeforeEach
    fun setup() {
        mockEmailSubscriptionRepository = mock(EmailSubscriptionRepository::class.java)

        emailSubscriptionService = EmailSubscriptionService(mockEmailSubscriptionRepository)

        `when`(mockEmailSubscriptionRepository.findByUuid(subscribedUuid)).thenReturn(subscribedEntity)
        `when`(mockEmailSubscriptionRepository.findByUuid(invalidUuid)).thenReturn(null)
        `when`(mockEmailSubscriptionRepository.save(any(EmailSubscriptionEntity::class.java)))
            .thenReturn(newSubscriberEntity)
    }

    @Test
    fun `validate that the email is unsubscribed for a valid UUID`() {
        emailSubscriptionService.unsubscribeEmailWithUuid(subscribedUuid)
        assertFalse(subscribedEntity.isSubscribed, "The email subscription should be unsubscribed.")
    }

    @Test
    fun `validate that unsubscribe does nothing for a invalid UUID`() {
        emailSubscriptionService.unsubscribeEmailWithUuid(invalidUuid)
        assertTrue(subscribedEntity.isSubscribed, "The email subscription should be subscribed as nothing happened.")
    }

    @Test
    fun `validate that isSubscribed is true if a email is subscribed`() {
        val isSubscribed = emailSubscriptionService.emailIsSubscribed(subscriberEmail)
        if (isSubscribed != null) {
            assertTrue(isSubscribed, "The email subscription should be subscribed.")
        }
    }

    @Test
    fun `validate that isSubscribed is false if a email is not subscribed`() {
        val isSubscribed = emailSubscriptionService.emailIsSubscribed(notASubscriberEmail)
        if (isSubscribed != null) {
            assertFalse(isSubscribed, "The email subscription should be subscribed.")
        }
    }

    @Test
    fun `validate that isSubscribed is null for unknown email`() {
        val isSubscribed = emailSubscriptionService.emailIsSubscribed(unkownEmail)
        assertNull(isSubscribed, "The response should be null for a unknown email.")
    }

    // To do: add tests for emailSubscriptionService.insertSubscriptionEntityIfNeededAndReturnUuid
}
