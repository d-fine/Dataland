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
import org.dataland.datalandbackendutils.model.QaStatus
import java.util.UUID

/**
 * The canonical backend persistence entity for the non-sourceability lifecycle.
 * This is the single source of truth for non-sourceability state; [SourceabilityEntity]
 * is retained as backup-only and must not be used as a runtime source.
 */
@Entity
@Table(name = "non_sourceability_information")
data class NonSourceabilityInformationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "non_sourceability_id", nullable = false, updatable = false)
    val nonSourceabilityId: UUID? = null,
    @Column(name = "company_id", nullable = false)
    val companyId: String,
    @Column(name = "data_type", nullable = false)
    @Convert(converter = DataTypeConverter::class)
    val dataType: DataType,
    @Column(name = "reporting_period", nullable = false)
    val reportingPeriod: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "qa_status", nullable = false)
    var qaStatus: QaStatus,
    @Column(name = "uploader_user_id", nullable = false)
    val uploaderUserId: String,
    @Column(name = "upload_time", nullable = false)
    val uploadTime: Long,
    @Column(name = "currently_active", nullable = false)
    var currentlyActive: Boolean,
    @Column(name = "reason", nullable = true)
    val reason: String?,
    @Column(name = "bypass_qa", nullable = false)
    val bypassQa: Boolean,
)
