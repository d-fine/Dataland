package org.dataland.datalandcommunitymanager.model.dataRequest

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum


/**
 * Contains info about the message part of the stored data request on Dataland.
 * @param contactList list of contact emails the user provided,
 * @param message message the user who created in the data request
 * @param dataType is the enum type of the framework for which the user requested data

 */
data class StoredMessageRequest (
    val contactList: List<String>,
    val message: String,
    val dataType: DataTypeEnum?,
)
