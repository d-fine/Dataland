package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

/**
 * Comparable to the ReviewQueueEntity.
 * This class is used or the GET Response.
 */
interface ReviewQueueResponse {
    val dataId: String
    val companyId: String?
    val framework: String?
    val reportingPeriod: String?
    val receptionTime: Long
    var companyName: String?
    var message: String?
}
