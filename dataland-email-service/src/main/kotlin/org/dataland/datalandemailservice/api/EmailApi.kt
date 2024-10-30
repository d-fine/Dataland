package org.dataland.datalandemailservice.api

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

/**
 * Defines the restful dataland mail service API
 */

interface EmailApi {
    /**
     * Unsubscribes a user from email communications based on the provided UUID.
     *
     * @param subscriptionId The UUID of the subscription to be unsubscribed.
     * @return A response entity indicating the result of the unsubscription.
     */
    @Operation(
        summary = "Unsubscribe from email communications",
        // TODO Emanuel: Unsubscribed nur die E-Mail-Adresse und keinen "user" => Wortlaut ändern
        description = "Unsubscribes a user from email communications based on the provided UUID.",
    )
    @PatchMapping(value = ["/subscriptions/{subscriptionId}"])
    fun unsubscribeUuid(
        @PathVariable("subscriptionId") subscriptionId: UUID,
    ): ResponseEntity<String>
}
