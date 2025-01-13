package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.converter.QaStatusConverter
import org.dataland.datalandbackendutils.interfaces.DataPointDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointQaReviewInformation
import java.util.UUID

/**
 * The entity storing the changes in the QA status over time for each individual data point
 */
@Entity
@Table(name = "data_point_qa_review")
data class DataPointQaReviewEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val eventId: UUID? = null,
    @Column(name = "data_id", nullable = false)
    val dataId: String,
    @Column(name = "company_id", nullable = false)
    override val companyId: String,
    @Column(name = "company_name", nullable = false)
    val companyName: String,
    @Column(name = "data_point_identifier", nullable = false)
    override val dataPointType: String,
    @Column(name = "reporting_period", nullable = false)
    override val reportingPeriod: String,
    @Column(name = "timestamp", nullable = false)
    val timestamp: Long,
    @Convert(converter = QaStatusConverter::class)
    @Column(name = "qa_status", nullable = false)
    var qaStatus: QaStatus,
    @Column(name = "triggering_user_id", nullable = false)
    val triggeringUserId: String,
    @Column(name = "comment", columnDefinition = "TEXT", nullable = true)
    val comment: String?,
) : DataPointDimensions {
    /**
     * Converts the entity into a DataPointQaReviewInformation object, which is used for API responses
     */
    fun toDataPointQaReviewInformation() =
        DataPointQaReviewInformation(
            dataId = this.dataId,
            companyId = this.companyId,
            companyName = this.companyName,
            dataPointType = this.dataPointType,
            reportingPeriod = this.reportingPeriod,
            timestamp = this.timestamp,
            qaStatus = this.qaStatus,
            comment = this.comment,
            reviewerId = this.triggeringUserId,
        )
}
