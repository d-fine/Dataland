package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
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

    val contacts: String?,

    @Column(columnDefinition = "TEXT")
    val message: String?,

    val lastModifiedDate: Long,

    @ManyToOne(optional = false)
    @JoinColumn(name = "data_request")
    var dataRequest: DataRequestEntity?,
) {
    init {
        require(contacts?.isNotEmpty() ?: true)
        require(message?.isNotEmpty() ?: true)
    }

    constructor(
        messageObject: StoredDataRequestMessageObject,
        ordinal: Int,
        dataRequest: DataRequestEntity? = null
    ) : this(
        messageId = UUID.randomUUID().toString(),
        ordinal = ordinal,
        contacts = if(messageObject.contactList.isEmpty()) null else messageObject.contactList.joinToString(";"),
        message = messageObject.message,
        lastModifiedDate = messageObject.lastModifiedDate ?: Instant.now().toEpochMilli(),
        dataRequest = dataRequest,
    )

    fun toStoredDataRequestMessageObject() = StoredDataRequestMessageObject(
        contactList = contacts?.split(";") ?: emptyList(),
        message  = message,
        lastModifiedDate = lastModifiedDate,
    )
}