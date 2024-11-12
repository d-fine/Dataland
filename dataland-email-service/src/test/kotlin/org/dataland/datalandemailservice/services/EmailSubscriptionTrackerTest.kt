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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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

    companion object {
        const val UNKNOWN_EMAIL = "unknown@mail.com"
        const val UNKNOWN_EMAIL_ALIAS = "uNknOwN@MaiL.coM"
        const val SUBSCRIBED_EMAIL = "subscriber@mail.com"
        const val SUBSCRIBED_EMAIL_ALIAS = "SubscRiber@mail.COM"
        const val UNSUBSCRIBED_EMAIL = "unsubscriber@mail.com"
        const val UNSUBSCRIBED_EMAIL_ALIAS = "UNSUBSCRIBER@MAIL.com"
    }

    val subscribedEntity =
        EmailSubscriptionEntity(
            uuid = subscribedUuid,
            emailAddress = SUBSCRIBED_EMAIL,
            isSubscribed = true,
        )

    val unsubscribedEntity =
        EmailSubscriptionEntity(
            uuid = unsubscribedUuid,
            emailAddress = UNSUBSCRIBED_EMAIL,
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
        val entity = emailSubscriptionTracker.getOrAddSubscription(UNKNOWN_EMAIL)
        val unknownEntity = emailSubscriptionRepository.findByEmailAddress(UNKNOWN_EMAIL)

        assertNotNull(unknownEntity)
        assertTrue(unknownEntity!!.isSubscribed)
        assertEquals(UNKNOWN_EMAIL, unknownEntity.emailAddress)
        assertEquals(entity.uuid, unknownEntity.uuid)
    }

    @Test
    fun `validate that no entity is created for subscribed address`() {
        val entity = emailSubscriptionTracker.getOrAddSubscription(SUBSCRIBED_EMAIL)

        assertEquals(subscribedUuid, entity.uuid)
        assertTrue(entity.isSubscribed)
        assertEquals(SUBSCRIBED_EMAIL, entity.emailAddress)
    }

    @Test
    fun `validate that no entity is created for unsubscribed address`() {
        val entity = emailSubscriptionTracker.getOrAddSubscription(UNSUBSCRIBED_EMAIL)

        assertEquals(unsubscribedEntity.uuid, entity.uuid)
        assertFalse(entity.isSubscribed)
        assertEquals(UNSUBSCRIBED_EMAIL, entity.emailAddress)
    }

    @Test
    fun `validate that contacts are partition correctly`() {
        val partitionedContacts =
            emailSubscriptionTracker.subscribeContactsIfNeededAndPartition(
                listOf(
                    EmailContact(SUBSCRIBED_EMAIL), EmailContact(UNSUBSCRIBED_EMAIL), EmailContact(UNKNOWN_EMAIL),
                ),
            )
        val unknownEntity = emailSubscriptionRepository.findByEmailAddress(UNKNOWN_EMAIL)
        assertNotNull(unknownEntity)
        assertTrue(unknownEntity!!.isSubscribed)

        assertEquals(1, partitionedContacts.blocked.size)
        assertEquals(EmailContact(UNSUBSCRIBED_EMAIL), partitionedContacts.blocked.first())

        assertEquals(
            mapOf(
                EmailContact(UNKNOWN_EMAIL) to unknownEntity.uuid,
                EmailContact(SUBSCRIBED_EMAIL) to subscribedUuid,
            ),
            partitionedContacts.allowed,
        )
    }

    @ParameterizedTest
    @ValueSource(strings = [SUBSCRIBED_EMAIL, SUBSCRIBED_EMAIL_ALIAS, UNKNOWN_EMAIL, UNKNOWN_EMAIL_ALIAS])
    fun `validate that subscribed and unknown emails should receive email`(email: String) {
        val emailContact = EmailContact.create(email)
        assertTrue(emailSubscriptionTracker.shouldReceiveEmail(emailContact))
    }

    @ParameterizedTest
    @ValueSource(strings = [UNSUBSCRIBED_EMAIL, UNSUBSCRIBED_EMAIL_ALIAS, "ceo@example.com"])
    fun `validate that unsubscribed emails and emails with example domain should not receive email`(email: String) {
        val emailContact = EmailContact.create(email)
        assertFalse(emailSubscriptionTracker.shouldReceiveEmail(emailContact))
    }
}
