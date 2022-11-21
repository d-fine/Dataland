package org.dataland.datalandapikeymanager.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

/**
 * --- API model ---
 * Class for defining the meta info associated to an API key
 * @param username of the owning user
 * @param role defines the role of the user
 * @param expiryDate specifies how long the API key can be used
 */
data class ApiKeyMetaInfo(
    @field:JsonProperty(required = true)
    val username: String,

    @field:JsonProperty(required = true)
    val role: String,

    @field:JsonProperty(required = true)
    val expiryDate: LocalDate?,
)
