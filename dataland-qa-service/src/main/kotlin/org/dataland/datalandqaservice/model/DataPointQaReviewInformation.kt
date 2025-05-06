package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import org.dataland.datalandbackendutils.interfaces.DataPointDimensions
import org.dataland.datalandbackendutils.model.QaStatus

/**
 * API model for the QA review information of a data point building on the DataPointDimensions interface.
 * @param dataPointId the id of the data point this review item is for
 * @param companyId the id of the company this data point belongs to
 * @param companyName the name of the company this data point belongs to
 * @param dataPointType the identifier of the type of data point
 * @param reportingPeriod the reporting period of the data point
 * @param timestamp the time the review was uploaded
 * @param qaStatus the QA status of the data point as provided by the reviewer
 * @param comment a comment explaining the verdict
 * @param reviewerId the id of the user who uploaded the review
 */
data class DataPointQaReviewInformation(
    val dataPointId: String,
    override val companyId: String,
    val companyName: String,
    override val dataPointType: String,
    override val reportingPeriod: String,
    val timestamp: Long,
    val qaStatus: QaStatus,
    val comment: String?,
    val reviewerId: String?,
) : DataPointDimensions
