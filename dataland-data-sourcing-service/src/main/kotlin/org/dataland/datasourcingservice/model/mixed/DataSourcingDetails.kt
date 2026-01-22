package org.dataland.datasourcingservice.model.mixed

import org.dataland.datasourcingservice.model.enums.DataSourcingState
import java.time.LocalDate

/**
 * DTO for transferring data sourcing details associated with a request.
 */
data class DataSourcingDetails(
    val dataSourcingEntityId: String? = null,
    val dataSourcingState: DataSourcingState? = null,
    val dateOfNextDocumentSourcingAttempt: LocalDate? = null,
    val documentCollector: String? = null,
    val dataExtractor: String? = null,
)
