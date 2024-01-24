package org.dataland.datalandcommunitymanager.model.dataRequest

data class MessageRequest (
    var contactList: List<String>? = null,

    val message: String? = null,

    val updateTimestamp: Long? = null,
)