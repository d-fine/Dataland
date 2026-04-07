package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackend.converter.DataTypeConverter
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.enums.commons.QaNonSourceabilityStatus
import org.dataland.datalandbackend.model.metainformation.SourceabilityInfoResponse
import java.util.UUID

/**
 * The database entity for storing non-sourceability information requests.
 * Represents a claim that a specific company-dataType-reportingPeriod combination cannot be sourced.
 */
@Entity
@Table(
    name = "non_sourceability_information",
)
data class NonSourceabilityInformationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "non_sourceability_id", nullable = false, updatable = false)
    val nonSourceabilityId: UUID?,
    @Column(name = "company_id", nullable = false)
    var companyId: String,
    @Column(name = "data_type", nullable = false)
    @Convert(converter = DataTypeConverter::class)
    var dataType: DataType,
    @Column(name = "reporting_period", nullable = false)
    var reportingPeriod: String,
    @Column(name = "reason", nullable = true)
    var reason: String?,
    @Column(name = "uploader_user_id", nullable = false)
    var uploaderUserId: String,
    @Column(name = "upload_time", nullable = false)
    var uploadTime: Long,
    @Column(name = "qa_status", nullable = false)
    @Enumerated(EnumType.STRING)
    var qaStatus: QaNonSourceabilityStatus,
    @Column(name = "currently_active", nullable = false)
    var currentlyActive: Boolean,
    @Column(name = "bypass_qa", nullable = false)
    var bypassQa: Boolean,
    @Column(name = "created_at", nullable = false)
    var createdAt: Long,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Long,
) {
    /**
     * Converts the entity to the API response model.
     */
    fun toApiModel(): SourceabilityInfoResponse =
        SourceabilityInfoResponse(
            companyId = companyId,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            reason = reason ?: "",
            creationTime = createdAt,
            userId = uploaderUserId,
            isNonSourceable = currentlyActive,
            nonSourceabilityId = nonSourceabilityId,
            uploaderUserId = uploaderUserId,
            uploadTime = uploadTime,
            qaStatus = qaStatus.name,
            currentlyActive = currentlyActive,
        )
}
