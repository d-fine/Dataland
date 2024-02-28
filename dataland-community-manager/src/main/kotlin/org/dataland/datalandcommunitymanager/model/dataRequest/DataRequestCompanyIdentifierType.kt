package org.dataland.datalandcommunitymanager.model.dataRequest

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum for the different identifier types that an identifier value of a stored data request can have.
 */
@Schema(
    enumAsRef = true,
)
enum class DataRequestCompanyIdentifierType {
    Lei,
    Isin,
    PermId,
    MultipleRegexMatches,
    DatalandCompanyId,
}
