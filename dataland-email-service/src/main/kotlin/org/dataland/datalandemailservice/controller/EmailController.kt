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
     * Unsubscribes an email address from email communications based on the provided UUID and sends a mail
     * to the stakeholders that this email address is now unsubscribed.
     * @param subscriptionId The UUID of the subscription to be unsubscribed.
     * @return A response entity indicating the result of the unsubscription.
     */
    @Transactional
    override fun unsubscribeUuid(subscriptionId: UUID): ResponseEntity<String> {
        // TODO log is misleading, as the person unsubscribing is probably no Dataland user
        logger.info("Received request to unsubscribe user with UUID: $subscriptionId")

        val emailSubscription = emailSubscriptionRepository.findByUuid(subscriptionId)

        if (emailSubscription != null) {
            emailSubscriptionService.unsubscribeEmailWithUuid(emailSubscription.uuid)
            // TODO log is misleading, as the person unsubscribing is probably no Dataland user
            logger.info("User with UUID: $subscriptionId has been successfully unsubscribed")

            unsubscriptionEmailToStakeholdersSender.sendUnsubscriptionEmail(emailSubscription.emailAddress)
            // TODO log is misleading, as the person unsubscribing is probably no Dataland user
            logger.info("Stakeholders have been informed that user with UUID: $subscriptionId has unsubscribed")
            return ResponseEntity.ok(emailSubscription.emailAddress + " has been successfully unsubscribed.")
        } else {
            logger.info("No user with UUID: $subscriptionId exists")
            return ResponseEntity.ok("There is no email address associated with the subscriptionId $subscriptionId.")
        }
    } // TODO logic should be in own serice e.g. "SubscriptionManager".
    // TODO a controller should only map the endpoint to some functionality somewhere
}
