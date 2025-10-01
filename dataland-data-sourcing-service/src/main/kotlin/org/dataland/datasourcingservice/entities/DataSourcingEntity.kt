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
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.datasourcing.ReducedDataSourcing
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
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
        UniqueConstraint(columnNames = ["company_id", "reporting_period", "data_type"]),
    ],
)
class DataSourcingEntity(
    @Id
    @Column(name = "data_sourcing_id")
    val dataSourcingId: UUID,
    @Column(name = "company_id")
    val companyId: UUID,
    @Column(name = "reporting_period")
    val reportingPeriod: String,
    @Column(name = "data_type")
    val dataType: String,
    @Column(name = "state")
    var state: DataSourcingState,
    @NotAudited
    @ElementCollection
    @Column(name = "document_id")
    var documentIds: Set<String> = emptySet(),
    @NotAudited
    @ElementCollection
    @Column(name = "expected_publication_date_documents")
    var expectedPublicationDatesOfDocuments: Set<ExpectedPublicationDateOfDocument> = emptySet(),
    @Column(name = "date_of_next_document_sourcing_attempt")
    var dateOfNextDocumentSourcingAttempt: LocalDate? = null,
    @Column(name = "document_collector")
    var documentCollector: UUID? = null,
    @Column(name = "data_extractor")
    var dataExtractor: UUID? = null,
    @Column(name = "admin_comment", length = 1000)
    var adminComment: String? = null,
    @NotAudited
    @OneToMany(mappedBy = "dataSourcingEntity", cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JsonManagedReference
    var associatedRequests: MutableSet<RequestEntity> = mutableSetOf(),
) {
    constructor(
        companyId: UUID,
        reportingPeriod: String,
        dataType: String,
    ) : this(
        dataSourcingId = UUID.randomUUID(),
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
            dataSourcingId = dataSourcingId.toString(),
            companyId = companyId.toString(),
            reportingPeriod = reportingPeriod,
            dataType = dataType,
            state = state,
            documentIds = documentIds,
            expectedPublicationDatesOfDocuments = expectedPublicationDatesOfDocuments,
            dateOfNextDocumentSourcingAttempt = dateOfNextDocumentSourcingAttempt,
            documentCollector = documentCollector?.toString(),
            dataExtractor = dataExtractor?.toString(),
            adminComment = adminComment,
            associatedRequestIds = associatedRequests.map { it.id.toString() }.toMutableSet(),
        )

    /**
     * Converts this DataSourcingEntity to a ReducedDataSourcing dto.
     */
    fun toReducedDataSourcing(): ReducedDataSourcing =
        ReducedDataSourcing(
            dataSourcingId = dataSourcingId.toString(),
            companyId = companyId.toString(),
            reportingPeriod = reportingPeriod,
            dataType = dataType,
            state = state,
            documentIds = documentIds,
            expectedPublicationDatesOfDocuments = expectedPublicationDatesOfDocuments,
            dateOfNextDocumentSourcingAttempt = dateOfNextDocumentSourcingAttempt,
        )

    /**
     * Converts this DataSourcingEntity to a DataSourcingWithoutReferences dto.
     */
    fun toDataSourcingWithoutReferences(): DataSourcingWithoutReferences =
        DataSourcingWithoutReferences(
            dataSourcingId = dataSourcingId.toString(),
            companyId = companyId.toString(),
            reportingPeriod = reportingPeriod,
            dataType = dataType,
            state = state,
            dateOfNextDocumentSourcingAttempt = dateOfNextDocumentSourcingAttempt,
            documentCollector = documentCollector?.toString(),
            dataExtractor = dataExtractor?.toString(),
            adminComment = adminComment,
        )
}
