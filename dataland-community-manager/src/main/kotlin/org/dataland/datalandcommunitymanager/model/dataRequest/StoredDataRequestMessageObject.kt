package org.dataland.datalandcommunitymanager.model.dataRequest

/**
 * --- API model ---
 * Contains info about a stored message object on Dataland.
 * @param contacts  a list of strings which include all contact (mail) details
 * @param message a string of all messages which were created during the life cycle
 * @param creationTimestamp the creation time of the message object
 */
data class StoredDataRequestMessageObject(
    var contacts: Set<String>,
    val message: String?,
    val creationTimestamp: Long,
)
