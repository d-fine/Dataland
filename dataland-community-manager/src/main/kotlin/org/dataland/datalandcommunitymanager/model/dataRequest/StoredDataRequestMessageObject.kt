package org.dataland.datalandcommunitymanager.model.dataRequest

data class StoredDataRequestMessageObject(

    /**
     * Contains info about a stored message object on Dataland.
     * @param contactList  a list of strings which include all contact (mail) details
     * @param message a string of all messages which were created during the life cycle
     * @param updateTimestamp the date when the data request has been modified the last time
     */

    var contactList: List<String>? = null,

    val message: String? = null,

    val updateTimestamp: Long? = null,
)
