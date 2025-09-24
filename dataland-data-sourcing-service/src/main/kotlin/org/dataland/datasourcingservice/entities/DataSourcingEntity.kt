package org.dataland.datasourcingservice.entities

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.hibernate.envers.Audited
import java.time.LocalDate
import java.util.UUID

/**
 * The database entity for storing data sourcing objects.
 */
@SuppressWarnings("LongParameterList")
@Entity
@Audited
@Table(
    name = "data_sourcing",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["companyId", "reportingPeriod", "dataType"]),
    ],
)
data class DataSourcingEntity(
    @Id
    @Column(name = "id")
    val id: UUID,
    @Column(name = "company_id")
    val companyId: UUID,
    @Column(name = "reporting_period")
    val reportingPeriod: String,
    @Column(name = "data_type")
    val dataType: String,
    @Column(name = "state")
    var state: DataSourcingState,
    @ElementCollection
    @Column(name = "document_id")
    var documentIds: Set<String> = emptySet(),
    @ElementCollection
    @Column(name = "expected_publication_date_documents")
    var expectedPublicationDatesOfDocuments: Set<ExpectedPublicationDateOfDocument> = emptySet(),
    @Column(name = "date_document_sourcing_attempt")
    var dateDocumentSourcingAttempt: LocalDate? = null,
    @Column(name = "document_collector")
    var documentCollector: UUID? = null,
    @Column(name = "data_extractor")
    var dataExtractor: UUID? = null,
    @Column(name = "admin_comment", length = 1000)
    var adminComment: String? = null,
    @OneToMany(mappedBy = "dataSourcingEntity", cascade = [CascadeType.PERSIST])
    @JsonManagedReference
    var associatedRequests: MutableSet<RequestEntity> = mutableSetOf(),
) {
    constructor(
        companyId: UUID,
        reportingPeriod: String,
        dataType: String,
    ) : this(
        id = UUID.randomUUID(),
        companyId = companyId,
        reportingPeriod = reportingPeriod,
        dataType = dataType,
        state = DataSourcingState.Initialized,
    )

    /**
     * Add an associated request to this data sourcing entity.
     * Make sure the data sourcing entity is also added to the request.
     */
    fun addAssociatedRequest(request: RequestEntity) {
        associatedRequests.add(request)
        request.dataSourcingEntity = this
    }

    /**
     * Converts this DataSourcingEntity to a StoredDataSourcing.
     */
    fun toStoredDataSourcing(): StoredDataSourcing =
        StoredDataSourcing(
            id = id.toString(),
            companyId = companyId.toString(),
            reportingPeriod = reportingPeriod,
            dataType = dataType,
            state = state,
            documentIds = documentIds,
            expectedPublicationDatesOfDocuments = expectedPublicationDatesOfDocuments,
            dateDocumentSourcingAttempt = dateDocumentSourcingAttempt,
            documentCollector = documentCollector.toString(),
            dataExtractor = dataExtractor.toString(),
            adminComment = adminComment,
            associatedRequestIds = associatedRequests.map { it.id.toString() }.toMutableSet(),
        )
}
