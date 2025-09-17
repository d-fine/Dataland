package org.dataland.datasourcingservice.entities

import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import java.util.Date
import java.util.UUID

/**
 * The database entity for storing data sourcing objects.
 */
@SuppressWarnings("LongParameterList")
@Entity
@Table(name = "data_sourcing")
class DataSourcingEntity(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID,
    @Column(name = "company_id")
    val companyId: UUID,
    @Column(name = "reporting_period")
    val reportingPeriod: String,
    @Column(name = "data_type")
    val dataType: String,
    @Column(name = "state")
    val state: DataSourcingState,
    @ElementCollection
    @Column(name = "document_id")
    val documentIds: Set<UUID>? = null,
    @ElementCollection
    @Column(name = "expected_publication_date_of_documents")
    val expectedPublicationDatesOfDocuments: Set<ExpectedPublicationDateOfDocument>? = null,
    @Column(name = "date_document_sourcing_attempt")
    val dateDocumentSourcingAttempt: Date? = null,
    @Column(name = "document_collector")
    val documentCollector: UUID? = null,
    @Column(name = "data_extractor")
    val dataExtractor: UUID? = null,
    @Column(name = "admin_comment", length = 1000)
    var adminComment: String? = null,
)
