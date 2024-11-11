package org.dataland.datalandemailservice.services

import jakarta.transaction.Transactional
import org.dataland.datalandemailservice.DatalandEmailService
import org.dataland.datalandemailservice.entities.EmailSubscriptionEntity
import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
import org.junit.jupiter.api.BeforeEach
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

    /*
    @Test
    fun `validate if a new subscription entity is created for an unknown email`() {
        val uuid = emailSubscriptionTracker.addSubscriptionIfNeededAndReturnUuid(unknownEmail)
        val newEntity = emailSubscriptionRepository.findByEmailAddress(unknownEmail)
        if (newEntity != null) {
            assertTrue(newEntity.isSubscribed, "The email subscription should be unsubscribed.")
            assertEquals(unknownEmail, newEntity.emailAddress)
            assertEquals(uuid, newEntity.uuid)
        } else {
            error("The new subscription entity should not be null.")
        }
    }

    @Test
    fun `validate that the uuid is returned for a subscribed email`() {
        val uuid = emailSubscriptionTracker.addSubscriptionIfNeededAndReturnUuid(subscribedEmail)
        assertEquals(subscribedUuid, uuid)
    }

    @Test
    fun `validate that a uuid is returned for an unsubscribed email address`() {
        val uuid = emailSubscriptionTracker.addSubscriptionIfNeededAndReturnUuid(unsubscribedEmail)
        assertEquals(unsubscribedUuid, uuid)
    }

    @Test
    fun `validate that a uuid is returned for a unknown email address`() {
        val uuid = emailSubscriptionTracker.addSubscriptionIfNeededAndReturnUuid(unknownEmail)
        val uuidString = uuid.toString()
        val convertedUuid = UUID.fromString(uuidString)

        assertEquals(uuid, convertedUuid)
    }

    @Test
    fun `validate that a subscribed email address is identified as subscribed`() {
        assertTrue(emailSubscriptionTracker.shouldReceiveEmail(subscribedEmail))
    }

    @Test
    fun `validate that a unsubscribed email address is not identified as subscribed`() {
        assertFalse(emailSubscriptionTracker.shouldReceiveEmail(unsubscribedEmail))
    }

    @Test
    fun `validate that an unknown email address is not identified as subscribed`() {
        assertTrue(emailSubscriptionTracker.shouldReceiveEmail(unknownEmail))
    }*/
}
