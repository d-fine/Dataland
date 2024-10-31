package org.dataland.datalandemailservice.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

/**
 * Defines the restful dataland mail service API
 */

interface EmailApi {
    /**
     * Unsubscribes an email address from email communications based on the provided UUID.
     *
     * @param subscriptionId is a UUID that is associated with the email address to be unsubscribed
     * @return A response entity indicating the result of the unsubscription request
     */
    @Operation(
        summary = "Unsubscribe from email communications",
        description = "Unsubscribes an email address from email communications based on the provided UUID.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully unsubscribed associated email address."),
        ],
    )
    @PatchMapping(
        value = ["/subscriptions/{subscriptionId}"],
    )
    fun unsubscribeUuid(
        @PathVariable("subscriptionId") subscriptionId: UUID,
    ): ResponseEntity<String>
}
