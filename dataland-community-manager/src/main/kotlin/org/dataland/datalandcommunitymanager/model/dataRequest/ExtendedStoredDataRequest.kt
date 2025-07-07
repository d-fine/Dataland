package org.dataland.datalandcommunitymanager.model.dataRequest

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity

/**
 * --- API model ---
 * Contains info about an extended stored data request on Dataland.
 * @param dataRequestId  unique identifier of the stored data request
 * @param userId the user who created this data request
 * @param creationTimestamp when the user created the data request
 * @param dataType is the enum type of the framework for which the user requested data
 * @param datalandCompanyId the value of the company identifier for this data request
 * @param companyName the name of the company for this data request
 * @param lastModifiedDate the date when the data request has been modified the last time
 * @param requestStatus the current status of the data request
 * @param accessStatus the current access status of the data request
 * @param requestPriority the priority of the data request
 * @param adminComment the admin comment of the data request
 */
data class ExtendedStoredDataRequest(
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.DATA_REQUEST_ID_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.DATA_REQUEST_ID_EXAMPLE,
    )
    val dataRequestId: String,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    )
    val userId: String,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.USER_EMAIL_ADDRESS_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.USER_EMAIL_ADDRESS_EXAMPLE,
    )
    var userEmailAddress: String?,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.CREATION_TIMESTAMP_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.CREATION_TIMESTAMP_EXAMPLE,
    )
    val creationTimestamp: Long,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE,
    )
    val dataType: String,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val datalandCompanyId: String,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
    )
    val companyName: String,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.LAST_MODIFIED_DATE_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.LAST_MODIFIED_DATE_EXAMPLE,
    )
    val lastModifiedDate: Long,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_STATUS_DESCRIPTION,
    )
    val requestStatus: RequestStatus,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.ACCESS_STATUS_DESCRIPTION,
    )
    val accessStatus: AccessStatus,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_PRIORITY_DESCRIPTION,
    )
    val requestPriority: RequestPriority,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.ADMIN_COMMENT_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.ADMIN_COMMENT_EXAMPLE,
    )
    var adminComment: String?,
) {
    constructor(dataRequestEntity: DataRequestEntity, companyName: String, userEmailAddress: String?) : this(
        dataRequestEntity.dataRequestId,
        dataRequestEntity.userId,
        userEmailAddress,
        dataRequestEntity.creationTimestamp,
        dataRequestEntity.dataType,
        dataRequestEntity.reportingPeriod,
        dataRequestEntity.datalandCompanyId,
        companyName,
        dataRequestEntity.lastModifiedDate,
        dataRequestEntity.requestStatus,
        dataRequestEntity.accessStatus,
        dataRequestEntity.requestPriority,
        dataRequestEntity.adminComment,
    )
}
