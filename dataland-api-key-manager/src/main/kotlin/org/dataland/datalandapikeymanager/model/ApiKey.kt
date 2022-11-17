package org.dataland.datalandapikeymanager.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

/**
 * --- API model ---
 * Class for defining the data associated to an API key
 * @param username of the owning user
 * @param expiryDate specifies how long the API key can be used
 * @param apiKey is the actual API key
 */
data class ApiKey(
    @field:JsonProperty(required = true)
    val username: String,

    @field:JsonProperty(required = true)
    val expiryDate: LocalDate?,

    @field:JsonProperty(required = true)
    val apiKey: String
)
