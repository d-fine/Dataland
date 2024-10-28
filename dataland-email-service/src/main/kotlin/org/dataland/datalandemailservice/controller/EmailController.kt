package org.dataland.datalandemailservice.controller


import org.dataland.datalandemailservice.api.EmailApi
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import org.dataland.datalandemailservice.services.EmailSubscriptionService

/**
 * Controller for the email service API.
 */
@RestController
class EmailController(
    @Autowired val emailSubscriptionService: EmailSubscriptionService
) : EmailApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Unsubscribes a user from email communications based on the provided UUID.
     *
     * @param subscriptionUuid The UUID of the subscription to be unsubscribed.
     * @return A response entity indicating the result of the unsubscription.
     */
    @Transactional
    override fun unsubscribeUuid(subscriptionUuid: UUID): ResponseEntity<String> {
        logger.info("Received request to unsubscribe with UUID: $subscriptionUuid")

        // Perform the unsubscription
        emailSubscriptionService.unsubscribeEmailWithUuid(subscriptionUuid)

        return ResponseEntity.ok("Successfully unsubscribed")
    }
}