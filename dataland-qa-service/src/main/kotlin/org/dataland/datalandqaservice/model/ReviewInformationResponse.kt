package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import org.dataland.datalandbackendutils.model.QaStatus

/**
 * Comparable to the ReviewInformationEntity with the difference that the reviewerKeycloakId is optional.
 * This class is used or the GET Response.
 */
data class ReviewInformationResponse(
    val dataId: String,
    val companyName: String?,
    val framework: String?,
    val reportingPeriod: String?,
    val receptionTime: Long,
    var qaStatus: QaStatus,
    val reviewerKeycloakId: String?,
    var message: String?,
)
