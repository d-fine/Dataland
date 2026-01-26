package org.dataland.datasourcingservice.model.mixed

import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState

/**
 * DTO for creating and transferring request data along with associated data sourcing details.
 */
data class DataSourcingEnhancedRequest(
    val id: String,
    val companyId: String,
    val reportingPeriod: String,
    val dataType: String,
    val userId: String,
    val creationTimestamp: Long,
    val memberComment: String? = null,
    val adminComment: String? = null,
    val lastModifiedDate: Long,
    val requestPriority: RequestPriority,
    val state: RequestState,
    val dataSourcingDetails: DataSourcingDetails? = null,
    val companyName: String,
    val userEmailAddress: String? = null,
)
