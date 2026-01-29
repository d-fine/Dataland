package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.converter.QaStatusConverter
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import java.util.UUID

/**
 * The database entity for storing dataset reviews
 */
@SuppressWarnings("LongParameterList")
@Entity
@Table(name = "dataset_review")
class DataSetReviewEntity(
    @Id
    @Column(name = "dataset_review_id")
    val dataSetReviewId: UUID,
    @Column(name = "dateset_id", unique = true)
    val datasetId: UUID,
    @Column(name = "company_id")
    val companyId: UUID,
    @Column(name = "data_type")
    val dataType: String,
    @Column(name = "reporting_period")
    val reportingPeriod: String,
    @Convert(converter = QaStatusConverter::class)
    @Column(name = "status")
    var status: QaStatus = QaStatus.Pending,
    @Column(name = "reviewer_user_id")
    var reviewerUserId: String,
    @ElementCollection
    @Column(name = "preapproved_data_point_ids")
    var preapprovedDataPointIds: Set<UUID> = emptySet(),
    @OneToMany
    @JoinColumn(name = "dataset_review_id")
    var qaReports: MutableSet<DataPointQaReportEntity>,
    @ElementCollection
    @Column(name = "approved_data_point_ids")
    var approvedDataPointIds: Set<UUID> = emptySet(),
    @ElementCollection
    @Column(name = "approved_custom_data_point_ids")
    var approvedCustomDataPointIds: Map<UUID, String> = emptyMap(),
)
