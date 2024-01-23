package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/**
 * The entity storing the information considering the message part of the single data request
 */
@Entity
@Table(name = "request_messages")
data class MessageRequestEntity(

    @Id
    val messageRequestId: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "data_request_id")
    val dataRequestEntity: DataRequestEntity,

    @ElementCollection
    var contactList: List<String>? = null,

    val message: String? = null,

    val updateTimestamp: Long? = null,
)
