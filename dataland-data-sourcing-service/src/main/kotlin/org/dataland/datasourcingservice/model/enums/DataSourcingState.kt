package org.dataland.datasourcingservice.model.enums

import io.swagger.v3.oas.annotations.media.Schema

/**
 * A class that holds the states a data sourcing entry can be in
 */
@Schema(
    enumAsRef = true,
)
enum class DataSourcingState {
    Initialized,
    DocumentSourcing,
    DocumentSourcingDone,
    DataExtraction,
    DataVerification,
    NonSourceable,
    Answered,
}
