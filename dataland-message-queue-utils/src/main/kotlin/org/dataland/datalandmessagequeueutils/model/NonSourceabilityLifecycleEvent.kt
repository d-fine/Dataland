package org.dataland.datalandmessagequeueutils.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus

/**
 * Shared payload for non-sourceability lifecycle events exchanged between services.
 */
data class NonSourceabilityLifecycleEvent(
    val nonSourceabilityId: String,
    val companyId: String,
    val dataType: String,
    val reportingPeriod: String,
    val eventType: NonSourceabilityEventType,
    val isNonSourceable: Boolean = true,
    val qaStatus: QaStatus? = null,
    val currentlyActive: Boolean? = null,
    val reason: String? = null,
    val bypassQa: Boolean? = null,
    val uploaderUserId: String? = null,
    val uploadTime: Long? = null,
) {
    @get:JsonIgnore
    val basicDataDimensions: BasicDataDimensions
        get() = BasicDataDimensions(companyId = companyId, dataType = dataType, reportingPeriod = reportingPeriod)
}
