package org.dataland.datalandemailservice.controller

import org.dataland.datalandemailservice.api.EmailApi
import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
import org.dataland.datalandemailservice.services.EmailSubscriptionService
import org.dataland.datalandemailservice.services.UnsubscriptionEmailToStakeholdersSender
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the email service API.
 */
@RestController
class EmailController(
    @Autowired val emailSubscriptionService: EmailSubscriptionService,
    @Autowired private val emailSubscriptionRepository: EmailSubscriptionRepository,
    @Autowired private val unsubscriptionEmailToStakeholdersSender: UnsubscriptionEmailToStakeholdersSender,
) : EmailApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Unsubscribes a user from email communications based on the provided UUID and
     * sends a mail to the stakeholders that this email address is now unsubscribed.
     *
     * @param subscriptionUuid The UUID of the subscription to be unsubscribed.
     * @return A response entity indicating the result of the unsubscription.
     */
    @Transactional
    override fun unsubscribeUuid(subscriptionUuid: UUID): ResponseEntity<String> {
        logger.info("Received request to unsubscribe user with UUID: $subscriptionUuid")

        val emailSubscription = emailSubscriptionRepository.findByUuid(subscriptionUuid)

        if (emailSubscription != null) {
            emailSubscriptionService.unsubscribeEmailWithUuid(emailSubscription.uuid)
            logger.info("User with UUID: $subscriptionUuid has been successfully unsubscribed")

            unsubscriptionEmailToStakeholdersSender.sendUnsubscriptionEmail(emailSubscription.emailAddress)
            logger.info("Stakeholders have been informed that user with UUID: $subscriptionUuid has unsubscribed")
        } else {
            logger.info("No user with UUID: $subscriptionUuid exists")
        }

        return ResponseEntity.ok("Successfully unsubscribed")
    }
}
