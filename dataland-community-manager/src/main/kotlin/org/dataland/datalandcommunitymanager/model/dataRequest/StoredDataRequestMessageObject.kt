package org.dataland.datalandcommunitymanager.model.dataRequest

data class StoredDataRequestMessageObject(
    var contactList: List<String>? = null,

    val message: String? = null,

    val updateTimestamp: Long? = null,
)
