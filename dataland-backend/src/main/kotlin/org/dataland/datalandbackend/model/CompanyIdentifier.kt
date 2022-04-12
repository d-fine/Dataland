package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Class for defining the company identifiers as a part of company information
 * @param identifierType type of the identifier
 * @param identifierValue value of the identifier
 */
data class CompanyIdentifier(
    @field:JsonProperty("identifierType", required = true) val identifierType: IdentifierType,
    @field:JsonProperty("identifierValue", required = true) val identifierValue: String
) {
    /**
     * A class that holds Identifier Types for a company
     */
    enum class IdentifierType { LEI, ISIN, PERMId }
}
