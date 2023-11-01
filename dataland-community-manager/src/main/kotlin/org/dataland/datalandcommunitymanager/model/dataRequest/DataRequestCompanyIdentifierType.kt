package org.dataland.datalandbackend.model.enums.p2p

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum for the different identifier types that an identifier value of a stored data request can have.
 */
@Schema(
    enumAsRef = true, // TODO needed?
)
enum class DataRequestCompanyIdentifierType { // TODO add values so it won't be written with 1,2,3... to database ?
    Lei,
    Isin,
    PermId,
    MultipleRegexMatches,
    DatalandCompanyId,
}
