package org.dataland.datalandcommunitymanager.model.inquiry

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * --- API model ---
 * Contact inquiry submitted by an unauthenticated visitor via the Dataland contact form.
 * @param contactName full name of the person submitting the inquiry
 * @param organisation optional organisation of the person submitting the inquiry
 * @param contactEmail email address of the person submitting the inquiry
 * @param message the inquiry message
 */
data class InquiryData(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = "The full name of the person submitting the inquiry.",
        example = "Jane Doe",
    )
    val contactName: String,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = "The organisation of the person submitting the inquiry (optional).",
        example = "Acme Corp",
        nullable = true,
    )
    val organisation: String?,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = "The email address of the person submitting the inquiry.",
        example = "jane.doe@example.com",
    )
    val contactEmail: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = "The inquiry message.",
        example = "I would like to learn more about Dataland.",
    )
    val message: String,
)
