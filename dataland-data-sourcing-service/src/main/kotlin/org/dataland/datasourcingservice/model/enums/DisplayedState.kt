package org.dataland.datasourcingservice.model.enums

import io.swagger.v3.oas.annotations.media.Schema

/**
 * A class that holds the combined states of a request and its associated data sourcing entry, used for the "mixedState"
 * field in CombinedStateHistoryEntry.
 */
@Schema(
    enumAsRef = true,
)
enum class DisplayedState {
    Open,
    Withdrawn,
    Validated,
    DocumentSourcing,
    DocumentVerification,
    DataExtraction,
    DataVerification,
    NonSourceable,
    Done,
}
