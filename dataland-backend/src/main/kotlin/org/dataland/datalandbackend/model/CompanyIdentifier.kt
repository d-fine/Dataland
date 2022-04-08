package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Class for defining the company identifiers as a part of company information
 * @param type type of the identifier
 * @param value value of the identifier
 */
data class CompanyIdentifier(
    @field:JsonProperty("type", required = true) val type: IdentifierType,
    @field:JsonProperty("value", required = true) val value: String
) {
    /**
     * A class that holds Identifier Types for a company
     */
    enum class IdentifierType { Lei, Isin, PermId }
}
