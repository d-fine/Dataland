package org.dataland.datalandcommunitymanager.model.dataRequest

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains info about a stored data request on Dataland.
 * @param dataRequestId  unique identifier of the stored data request
 * @param userId the user who created this data request
 * @param notifyMeImmediately Flag if the user wants immediate updates or if a notification within the summary is sufficient
 * @param userEmailAddress contains the email address of the user who created this data request
 * @param creationTimestamp when the user created the data request
 * @param dataType is the enum type of the framework for which the user requested data
 * @param datalandCompanyId the value of the company identifier for this data request
 * @param lastModifiedDate the date when the data request has been modified the last time
 * @param messageHistory a list of all message objects which were created during the life cycle
 * @param requestStatus the current status of the data request
 * @param requestPriority the priority of the data request
 * @param adminComment the admin comment of the data request
 */
data class StoredDataRequest(
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.DATA_REQUEST_ID_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.DATA_REQUEST_ID_EXAMPLE,
    )
    val dataRequestId: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_REQUEST_USER_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    )
    val userId: String,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.DATA_REQUEST_USER_EMAIL_ADDRESS_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.USER_EMAIL_ADDRESS_EXAMPLE,
    )
    val userEmailAddress: String?,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.BULK_REQUEST_NOTIFY_ME_IMMEDIATELY_DESCRIPTION,
    )
    val notifyMeImmediately: Boolean = false,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.CREATION_TIMESTAMP_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.CREATION_TIMESTAMP_EXAMPLE,
    )
    val creationTimestamp: Long,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_FRAMEWORK_EXAMPLE,
    )
    val dataType: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val datalandCompanyId: String,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.MESSAGE_HISTORY_DESCRIPTION,
    )
    val messageHistory: List<StoredDataRequestMessageObject>,
    val dataRequestStatusHistory: List<StoredDataRequestStatusObject>,
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
    val adminComment: String?,
)
