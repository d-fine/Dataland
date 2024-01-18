package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

/**
 * The entity storing the information considering the message part of the single data request
 */
@Entity
@Table(name = "request_messages")
data class MessageRequestEntity(

    @Id
    val messageRequestId: String,

    @JoinColumn(name = "data_request_id")
    val dataRequestId: String,

    @ElementCollection
    var contactList: List<String>? = null,

    val message: String? = null,

    val updateTimestamp: Long? = null,
)
