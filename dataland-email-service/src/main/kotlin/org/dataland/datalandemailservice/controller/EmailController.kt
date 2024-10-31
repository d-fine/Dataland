package org.dataland.datalandemailservice.controller

import org.dataland.datalandemailservice.api.EmailApi
import org.dataland.datalandemailservice.services.UnsubscriptionService
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
    @Autowired val unsubscriptionService: UnsubscriptionService,
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
        logger.info("Received request to unsubscribe email corresponding to UUID: $subscriptionId")
        return unsubscriptionService.unsubscribeUuidAndSendMailToStakeholders(subscriptionId)
    }
}
