package org.dataland.datalandcommunitymanager.model.dataRequest

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains info about a stored message object on Dataland.
 * @param contacts  a list of strings which include all contact (mail) details
 * @param message a string of all messages which were created during the life cycle
 * @param creationTimestamp the creation time of the message object
 */
data class StoredDataRequestMessageObject(
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = CommunityManagerOpenApiDescriptionsAndExamples.CONTACTS_DESCRIPTION,
                example = CommunityManagerOpenApiDescriptionsAndExamples.CONTACTS_EXAMPLE,
            ),
    )
    var contacts: Set<String>,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.MESSAGE_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.MESSAGE_EXAMPLE,
    )
    val message: String?,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.MESSAGE_CREATION_TIMESTAMP_DESCRIPTION,
        example = CommunityManagerOpenApiDescriptionsAndExamples.MESSAGE_CREATION_TIMESTAMP_EXAMPLE,
    )
    val creationTimestamp: Long,
)
