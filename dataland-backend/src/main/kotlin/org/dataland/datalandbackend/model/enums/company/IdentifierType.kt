package org.dataland.datalandbackend.model.enums.company

import io.swagger.v3.oas.annotations.media.Schema

/**
 * A class that holds Identifier Types for a company
 */
@Schema(
    enumAsRef = true,
)
enum class IdentifierType { Lei, Isin, PermId, Ticker, Duns, CompanyRegistrationNumber, VatNumber }
