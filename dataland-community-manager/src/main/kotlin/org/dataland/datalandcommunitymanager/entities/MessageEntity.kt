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
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
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
        private const val EMAIL_SEPARATOR = ";"

        const val COMPANY_OWNER_KEYWORD = "COMPANY_OWNER"

        /**
         * This method checks if a contact is either a valid email address or represents the company owner
         * @param contact which should be checked if it is an email address or the company owner keyword
         * @return true if either it was confirmed that it is a email address or the company owner keyword
         */
        private fun isContact(
            contact: String,
            companyRolesManager: CompanyRolesManager,
            companyId: String,
        ): Boolean =
            contact.isEmailAddress() ||
                (
                    contact == COMPANY_OWNER_KEYWORD &&
                        companyRolesManager
                            .getCompanyRoleAssignmentsByParameters(
                                CompanyRole.CompanyOwner, companyId, null,
                            ).isNotEmpty()
                )

        /**
         * This method checks whether a given contact is valid or not. If it is not valid then an invalid input
         * exception is thrown
         * @param contact the contact which should be checked for validity
         */
        fun validateContact(
            contact: String,
            companyRolesManager: CompanyRolesManager,
            companyId: String,
        ) {
            if (!isContact(contact, companyRolesManager, companyId)) {
                throw InvalidInputApiException(
                    "Invalid contact $contact",
                    "The provided contact $contact is not valid. " +
                        "Please specify a valid email address or when a company owner exists $COMPANY_OWNER_KEYWORD.",
                )
            }
        }

        /**
         * This method adds a contact email address of the user id or a company owner to the email recipient list
         * @param contact is either an email address or the company owner keyword
         * @param companyRolesManager is the service to handle all tasks in regard to company roles for users
         * @param companyId the company id for which the company owner should be determined
         * @return a list containing email address and/or the user ids of relevant company owners
         */
        fun addContact(
            contact: String,
            companyRolesManager: CompanyRolesManager,
            companyId: String,
        ): List<EmailRecipient> =
            if (contact.isEmailAddress()) {
                listOf(EmailRecipient.EmailAddress(contact))
            } else if (contact == COMPANY_OWNER_KEYWORD) {
                val companyOwnerList =
                    companyRolesManager.getCompanyRoleAssignmentsByParameters(
                        companyRole = CompanyRole.CompanyOwner,
                        companyId = companyId,
                        userId = null,
                    )
                companyOwnerList.map { EmailRecipient.UserId(it.userId) }
            } else {
                listOf()
            }
    }

    init {
        require(contacts.isNotEmpty())
        require(contacts.split(EMAIL_SEPARATOR).all { it.isEmailAddress() || it == COMPANY_OWNER_KEYWORD })
        require(message?.isNotBlank() ?: true)
    }

    constructor(
        messageObject: StoredDataRequestMessageObject,
        dataRequest: DataRequestEntity,
    ) : this(
        messageId = UUID.randomUUID().toString(),
        contacts = messageObject.contacts.joinToString(EMAIL_SEPARATOR),
        message = messageObject.message,
        creationTimestamp = messageObject.creationTimestamp,
        dataRequest = dataRequest,
    )

    /**
     * Converts this entity to a message object
     * @returns the generated message object
     */
    fun toStoredDataRequestMessageObject() =
        StoredDataRequestMessageObject(
            contacts = contactsAsSet(),
            message = message,
            creationTimestamp = creationTimestamp,
        )

    /**
     * Returns the contacts as a set of strings.
     */
    fun contactsAsSet(): Set<String> = contacts.split(EMAIL_SEPARATOR).toSet()
}
