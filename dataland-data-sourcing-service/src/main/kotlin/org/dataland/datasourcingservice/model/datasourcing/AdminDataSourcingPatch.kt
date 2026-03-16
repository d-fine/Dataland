package org.dataland.datasourcingservice.model.datasourcing

import org.dataland.datasourcingservice.entities.ExpectedPublicationDateOfDocument
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import java.time.LocalDate

/**
 * Request body for the admin PATCH endpoint. All fields are nullable — null means the field is not updated.
 */
data class AdminDataSourcingPatch(
    val documentCollector: String? = null,
    val dataExtractor: String? = null,
    val adminComment: String? = null,
    val priority: Int? = null,
    val state: DataSourcingState? = null,
    val documentIds: Set<String>? = null,
    val expectedPublicationDatesOfDocuments: Set<ExpectedPublicationDateOfDocument>? = null,
    val dateOfNextDocumentSourcingAttempt: LocalDate? = null,
)
