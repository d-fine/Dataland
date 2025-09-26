package org.dataland.datasourcingservice.model.datasourcing

import org.dataland.datasourcingservice.entities.ExpectedPublicationDateOfDocument
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.request.StoredRequest
import java.time.LocalDate
import java.util.UUID

/**
 * Patch class for data sourcing entities.
 */
data class DataSourcingPatch(
    val state: DataSourcingState? = null,
    val documentIds: Set<String>? = null,
    val expectedPublicationDatesOfDocuments: Set<ExpectedPublicationDateOfDocument>? = null,
    val dateOfNextDocumentSourcingAttempt: LocalDate? = null,
    val documentCollector: UUID? = null,
    val dataExtractor: UUID? = null,
    val adminComment: String? = null,
    val associatedRequests: Set<StoredRequest>? = null,
)
