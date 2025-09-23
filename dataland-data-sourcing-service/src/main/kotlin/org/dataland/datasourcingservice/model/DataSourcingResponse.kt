package org.dataland.datasourcingservice.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.Date

/**
 * Contains all information a user receives regarding an existing DataSourcing object.
 *
 * \@param id Unique identifier of the data sourcing object.
 * \@param companyId Company ID associated with the data sourcing object.
 * \@param reportingPeriod Reporting period.
 * \@param dataType Type of data being sourced.
 * \@param state Current state of the data sourcing object.
 * \@param documentIds Document IDs associated with the data sourcing object.
 * \@param expectedPublicationDatesOfDocuments Expected publication dates of documents.
 * \@param dateDocumentSourcingAttempt Date of document sourcing attempt.
 * \@param documentCollector Document collector's ID.
 * \@param dataExtractor Data extractor's ID.
 * \@param adminComment Admin comment.
 * \@param associatedRequestIds Associated request IDs.
 */
@Schema(description = "Data transfer object for DataSourcingEntity.")
data class DataSourcingResponse(
    @field:Schema(description = "Unique identifier of the data sourcing object.")
    val id: String,
    @field:Schema(description = "Company ID associated with the data sourcing object.")
    val companyId: String,
    @field:Schema(description = "Reporting period.")
    val reportingPeriod: String,
    @field:Schema(description = "Type of data being sourced.")
    val dataType: String,
    @field:Schema(description = "Current state of the data sourcing object.")
    val state: String,
    @field:Schema(description = "Document IDs associated with the data sourcing object.")
    val documentIds: Set<String>,
    @field:Schema(description = "Expected publication dates of documents.")
    val expectedPublicationDatesOfDocuments: Set<Date>,
    @field:Schema(description = "Date of document sourcing attempt.")
    val dateDocumentSourcingAttempt: LocalDate?,
    @field:Schema(description = "Document collector's ID.")
    val documentCollector: String?,
    @field:Schema(description = "Data extractor's ID.")
    val dataExtractor: String?,
    @field:Schema(description = "Admin comment.")
    val adminComment: String?,
    @field:Schema(description = "Associated request IDs.")
    val associatedRequestIds: Set<String>,
)
