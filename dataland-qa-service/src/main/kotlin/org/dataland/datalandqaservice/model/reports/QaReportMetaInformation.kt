package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Meta information associated to a QA report in the QA data storage
 * @param dataId unique identifier to identify the data the report  is associated with
 * @param qaReportId unique identifier of the QA report
 * @param reporterUserId the user ID of the user who requested the upload of this QA report
 * @param uploadTime is a timestamp for the upload of this QA report
 */
data class QaReportMetaInformation(
    @field:JsonProperty(required = true)
    val dataId: String,
    @field:JsonProperty(required = true)
    val dataType: String,
    @field:JsonProperty(required = true)
    val qaReportId: String,
    @field:JsonProperty()
    val reporterUserId: String?,
    @field:JsonProperty(required = true)
    val uploadTime: Long,
    @field:JsonProperty(required = true)
    val active: Boolean,
)
