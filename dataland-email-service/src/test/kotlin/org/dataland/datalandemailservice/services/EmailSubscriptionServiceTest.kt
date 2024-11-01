package org.dataland.datalandemailservice.services

import jakarta.transaction.Transactional
import org.dataland.datalandemailservice.DatalandEmailService
import org.dataland.datalandemailservice.entities.EmailSubscriptionEntity
import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
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
class EmailSubscriptionServiceTest(
    @Autowired private val emailSubscriptionRepository: EmailSubscriptionRepository,
) {
    private lateinit var emailSubscriptionService: EmailSubscriptionService

    final val subscribedUuid = UUID.randomUUID()
    final val unsubscribedUuid = UUID.randomUUID()
    final val newUuid = UUID.randomUUID()

    final val subscribedEmail = "subscriber@mail.com"
    final val unsubscribedEmail = "notasubscriber@mail.com"
    final val unkownEmail = "unknown@mail.com"
    final val newEmail = "new@mail.com"

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

    val newlySubscribedEntity =
        EmailSubscriptionEntity(
            uuid = newUuid,
            emailAddress = newEmail,
            isSubscribed = true,
        )

    @BeforeEach
    fun setup() {
        emailSubscriptionRepository.deleteAll()
        emailSubscriptionRepository.save(subscribedEntity)
        emailSubscriptionRepository.save(unsubscribedEntity)
        emailSubscriptionRepository.save(newlySubscribedEntity)
    }

    @Test
    fun `validate that a new subscription is generated to a new email address`() {
        emailSubscriptionService.insertSubscriptionEntityIfNeededAndReturnUuid(unkownEmail)
        val entity = emailSubscriptionRepository.findByEmailAddress(unkownEmail)
        entity?.isSubscribed?.let { assertTrue(it, "The email subscription should be subscribed.") }
    }
}
