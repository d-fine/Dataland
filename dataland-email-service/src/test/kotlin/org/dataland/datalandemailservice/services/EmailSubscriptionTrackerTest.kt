package org.dataland.datalandemailservice.services

import jakarta.transaction.Transactional
import org.dataland.datalandemailservice.DatalandEmailService
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.entities.EmailSubscriptionEntity
import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
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
    fun `validate if a new subscription entity is created for an unknown email`() {
        val entity = emailSubscriptionTracker.getOrAddSubscription(unknownEmail)
        val unknownEntity = emailSubscriptionRepository.findByEmailAddress(unknownEmail)

        assertNotNull(unknownEntity)
        assertTrue(unknownEntity!!.isSubscribed)
        assertEquals(unknownEmail, unknownEntity.emailAddress)
        assertEquals(entity.uuid, unknownEntity.uuid)
    }

    @Test
    fun `validate that no entity is created for subscribed address`() {
        val entity = emailSubscriptionTracker.getOrAddSubscription(subscribedEmail)

        assertEquals(subscribedUuid, entity.uuid)
        assertTrue(entity.isSubscribed)
        assertEquals(subscribedEmail, entity.emailAddress)
    }

    @Test
    fun `validate that no entity is created for unsubscribed address`() {
        val entity = emailSubscriptionTracker.getOrAddSubscription(unsubscribedEmail)

        assertEquals(unsubscribedEntity.uuid, entity.uuid)
        assertFalse(entity.isSubscribed)
        assertEquals(unsubscribedEmail, entity.emailAddress)
    }

    @Test
    fun `validate that contacts are partition correctly`() {
        val partitionedContacts =
            emailSubscriptionTracker.subscribeContactsIfNeededAndPartition(
                listOf(
                    EmailContact(subscribedEmail), EmailContact(unsubscribedEmail), EmailContact(unknownEmail),
                ),
            )
        val unknownEntity = emailSubscriptionRepository.findByEmailAddress(unknownEmail)
        assertNotNull(unknownEntity)
        assertTrue(unknownEntity!!.isSubscribed)

        assertEquals(1, partitionedContacts.blocked.size)
        assertEquals(EmailContact(unsubscribedEmail), partitionedContacts.blocked.first())

        assertEquals(
            mapOf(
                EmailContact(unknownEmail) to unknownEntity.uuid,
                EmailContact(subscribedEmail) to subscribedUuid,
            ),
            partitionedContacts.allowed,
        )
    }

    @Test
    fun `validate that a subscribed email address should receive email`() {
        assertTrue(emailSubscriptionTracker.shouldReceiveEmail(subscribedEmail))
    }

    @Test
    fun `validate that a unsubscribed email address should not receive email`() {
        assertFalse(emailSubscriptionTracker.shouldReceiveEmail(unsubscribedEmail))
    }

    @Test
    fun `validate that an unknown email address should receive email`() {
        assertTrue(emailSubscriptionTracker.shouldReceiveEmail(unknownEmail))
    }

    @Test
    fun `validate that example dot com domain does not receive email`() {
        assertFalse(emailSubscriptionTracker.shouldReceiveEmail("ceo@example.com"))
    }
}
