package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
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

    val ordinal: Int,

    @Column(columnDefinition = "TEXT")
    val contacts: String?,

    @Column(columnDefinition = "TEXT")
    val message: String?,

    val lastModifiedDate: Long,

    @ManyToOne(optional = false)
    @JoinColumn(name = "data_request_id")
    var dataRequest: DataRequestEntity?,
) {
    init {
        require(contacts?.isNotEmpty() ?: true)
        require(message?.isNotEmpty() ?: true)
    }

    constructor(
        messageObject: StoredDataRequestMessageObject,
        ordinal: Int,
        dataRequest: DataRequestEntity? = null,
    ) : this(
        messageId = UUID.randomUUID().toString(),
        ordinal = ordinal,
        contacts = if (messageObject.contacts.isEmpty()) null else messageObject.contacts.joinToString(";"),
        message = messageObject.message,
        lastModifiedDate = messageObject.lastModifiedDate ?: Instant.now().toEpochMilli(),
        dataRequest = dataRequest,
    )

    /**
     * Converts this entity to a message object
     * @returns the generated message object
     */
    fun toStoredDataRequestMessageObject() = StoredDataRequestMessageObject(
        contacts = contacts?.split(";")?.toSet() ?: emptySet(),
        message = message,
        lastModifiedDate = lastModifiedDate,
    )
}
