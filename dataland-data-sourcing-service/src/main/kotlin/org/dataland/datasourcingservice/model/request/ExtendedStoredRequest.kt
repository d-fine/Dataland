package org.dataland.datasourcingservice.model.request

import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState

/**
 * DTO for creating and transferring request data.
 */
data class ExtendedStoredRequest(
    val id: String,
    val companyId: String,
    val reportingPeriod: String,
    val dataType: String,
    val userId: String,
    val creationTimeStamp: Long,
    val memberComment: String? = null,
    val adminComment: String? = null,
    val lastModifiedDate: Long,
    val requestPriority: RequestPriority,
    val state: RequestState,
    val dataSourcingEntityId: String? = null,
    val companyName: String? = null,
    val userEmailAddress: String? = null,
)
