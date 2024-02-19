package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandemail.email.isEmailAddress
import java.time.Instant
import java.util.UUID

/**
 * The database entity for storing a single message in a data request conversation
 */
@Entity
@Table(name = "messages")
data class MessageEntity(
    @Id
    val messageId: String,

    @Column(columnDefinition = "TEXT")
    val contacts: String,

    @Column(columnDefinition = "TEXT")
    val message: String?,

    val creationTimestamp: Long,

    @ManyToOne(optional = false)
    @JoinColumn(name = "data_request_id")
    var dataRequest: DataRequestEntity,
) {
    companion object {
        private const val emailSeparator = ";"
    }

    init {
        require(contacts.isNotEmpty())
        require(contacts.split(emailSeparator).all { it.isEmailAddress() })
        require(message?.isNotBlank() ?: true)
    }

    constructor(
        messageObject: StoredDataRequestMessageObject,
        dataRequest: DataRequestEntity,
    ) : this(
        messageId = UUID.randomUUID().toString(),
        contacts = messageObject.contacts.joinToString(emailSeparator),
        message = messageObject.message,
        creationTimestamp = messageObject.creationTimestamp ?: Instant.now().toEpochMilli(),
        dataRequest = dataRequest,
    )

    /**
     * Converts this entity to a message object
     * @returns the generated message object
     */
    fun toStoredDataRequestMessageObject() = StoredDataRequestMessageObject(
        contacts = contacts.split(emailSeparator).toSet(),
        message = message,
        creationTimestamp = creationTimestamp,
    )
}
