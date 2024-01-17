package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.*

/**
 * The entity storing the information considering the message part of the single data request
 */
@Entity
@Table(name = "request_messages")
data class MessageRequestEntity (

    @Id
    val messageRequestId: String,

    @ElementCollection
    var contactList: List<String>?,

    val message: String,

    val updateTimestamp:  Long?,
)