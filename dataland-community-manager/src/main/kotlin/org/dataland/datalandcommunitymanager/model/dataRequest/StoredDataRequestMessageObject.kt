package org.dataland.datalandcommunitymanager.model.dataRequest

/**
 * Contains info about a stored message object on Dataland.
 * @param contacts  a list of strings which include all contact (mail) details
 * @param message a string of all messages which were created during the life cycle
 * @param lastModifiedDate the date when the data request has been modified the last time
 */
data class StoredDataRequestMessageObject(
    var contacts: Set<String>,

    val message: String? = null,

    val lastModifiedDate: Long? = null,
)
