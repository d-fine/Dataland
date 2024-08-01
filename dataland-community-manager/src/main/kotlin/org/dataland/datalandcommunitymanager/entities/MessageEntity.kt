package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.isEmailAddress
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.services.CompanyRolesManager
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
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

        const val COMPANY_OWNER_KEYWORD = "COMPANY_OWNER"

        fun isContact(contact: String) : Boolean {
            return contact.isEmailAddress() || contact == COMPANY_OWNER_KEYWORD
        }

        fun validateContact(contact: String) {
            // TODO do we want to check whether we want to validate if a company owner exists?

            if (!isContact(contact))
                throw InvalidInputApiException(
                    "Invalid contact $contact",
                    "The provided contact $contact is not valid. " +
                            "Please specify a valid email address or $COMPANY_OWNER_KEYWORD.")
        }

        fun realizeContact(contact: String, companyRolesManager: CompanyRolesManager, companyId: String
        ) : List<TemplateEmailMessage.EmailRecipient> {

            if (contact.isEmailAddress()) {
                return listOf(TemplateEmailMessage.EmailAddressEmailRecipient(contact))
            }

            if (contact == COMPANY_OWNER_KEYWORD) {
                val companyOwnerList = companyRolesManager.getCompanyRoleAssignmentsByParameters(
                    companyRole = CompanyRole.CompanyOwner,
                    companyId = companyId,
                    userId = null,
                )
                return companyOwnerList.map { TemplateEmailMessage.UserIdEmailRecipient(it.userId) }
            }

            return listOf()
        }

    }

    init {
        require(contacts.isNotEmpty())
        require(contacts.split(emailSeparator).all { isContact(it) })
        require(message?.isNotBlank() ?: true)
    }

    constructor(
        messageObject: StoredDataRequestMessageObject,
        dataRequest: DataRequestEntity,
    ) : this(
        messageId = UUID.randomUUID().toString(),
        contacts = messageObject.contacts.joinToString(emailSeparator),
        message = messageObject.message,
        creationTimestamp = messageObject.creationTimestamp,
        dataRequest = dataRequest,
    )

    /**
     * Converts this entity to a message object
     * @returns the generated message object
     */
    fun toStoredDataRequestMessageObject() = StoredDataRequestMessageObject(
        contacts = contactsAsSet(),
        message = message,
        creationTimestamp = creationTimestamp,
    )

    /**
     * Returns the contacts as a set of strings.
     */
    fun contactsAsSet(): Set<String> {
        return contacts.split(emailSeparator).toSet()
    }
}
