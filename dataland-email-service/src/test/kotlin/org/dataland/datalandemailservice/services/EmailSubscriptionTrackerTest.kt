package org.dataland.datalandemailservice.services

import jakarta.transaction.Transactional
import org.dataland.datalandemailservice.DatalandEmailService
import org.dataland.datalandemailservice.entities.EmailSubscriptionEntity
import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

@SpringBootTest(classes = [DatalandEmailService::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class EmailSubscriptionTrackerTest(
    @Autowired private val emailSubscriptionRepository: EmailSubscriptionRepository,
) {
    private lateinit var emailSubscriptionTracker: EmailSubscriptionTracker

    final val subscribedUuid = UUID.randomUUID()
    final val unsubscribedUuid = UUID.randomUUID()

    final val unknownEmail = "unknown@mail.com"
    final val subscribedEmail = "subscriber@mail.com"
    final val unsubscribedEmail = "unsubscriber@mail.com"

    val subscribedEntity =
        EmailSubscriptionEntity(
            uuid = subscribedUuid,
            emailAddress = subscribedEmail,
            isSubscribed = true,
        )

    val unsubscribedEntity =
        EmailSubscriptionEntity(
            uuid = unsubscribedUuid,
            emailAddress = unsubscribedEmail,
            isSubscribed = false,
        )

    @BeforeEach
    fun setup() {
        emailSubscriptionTracker = EmailSubscriptionTracker(emailSubscriptionRepository)
        emailSubscriptionRepository.deleteAll()
        emailSubscriptionRepository.save(subscribedEntity)
        emailSubscriptionRepository.save(unsubscribedEntity)
    }

    @Test
    fun `validate if a new subscription entity is created for a unknown email`() {
        val uuid = emailSubscriptionTracker.insertSubscriptionEntityIfNeededAndReturnUuid(unknownEmail)
        val newEntity = emailSubscriptionRepository.findByEmailAddress(unknownEmail)
        if (newEntity != null) {
            assertTrue(newEntity.isSubscribed, "The email subscription should be unsubscribed.")
            assertEquals(unknownEmail, newEntity.emailAddress)
            assertEquals(uuid, newEntity.uuid)
        }
    }

    @Test
    fun `validate if the uuid is returned for a known email`() {
        val uuid = emailSubscriptionTracker.insertSubscriptionEntityIfNeededAndReturnUuid(subscribedEmail)
        assertEquals(subscribedUuid, uuid)
    }

    @Test
    fun `validate that the uuid  is returned for unsubscriber`() {
        val uuid = emailSubscriptionTracker.insertSubscriptionEntityIfNeededAndReturnUuid(unsubscribedEmail)
        assertEquals(unsubscribedUuid, uuid)
    }

    @Test
    fun `validate that a subscribed email is returned as true`() {
        emailSubscriptionTracker.emailIsSubscribed(subscribedEmail)?.let { assertTrue(it) }
    }

    @Test
    fun `validate that a unsubscribed email is returned as false`() {
        emailSubscriptionTracker.emailIsSubscribed(unsubscribedEmail)?.let { assertFalse(it) }
    }

    @Test
    fun `validate that a unknown email is returned as null`() {
        emailSubscriptionTracker.emailIsSubscribed(unknownEmail)?.let { assertNull(it) }
    }
}
