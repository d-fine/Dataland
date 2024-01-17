package org.dataland.datalandcommunitymanager.model.dataRequest

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.hibernate.annotations.UpdateTimestamp


/**
 * Contains info about the message part of the stored data request on Dataland.
 * @param contactList list of contact emails the user provided,
 * @param message message the user who created in the data request
 * @param updateTimestamp is the time of last update of the message

 */
data class StoredMessageRequest (
    val contactList: List<String>,
    val message: String,
    val updateTimestamp:  Long?,
)
