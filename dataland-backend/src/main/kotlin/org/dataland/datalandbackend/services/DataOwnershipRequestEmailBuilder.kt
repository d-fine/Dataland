package org.dataland.datalandbackend.services

import org.dataland.datalandbackendutils.email.BaseEmailBuilder
import org.dataland.datalandbackendutils.email.Email
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * A class that manages generating emails regarding data ownership requests
 */
@Component
class DataOwnershipRequestEmailBuilder(
    @Value("\${dataland.proxy.primary.url}") private val proxyPrimaryUrl: String,
    @Value("\${dataland.notification.sender.address}") senderEmail: String,
    @Value("\${dataland.notification.sender.name}") senderName: String,
    @Value("\${dataland.notification.data-ownership-request.receivers}") semicolonSeparatedReceiverEmails: String,
    @Value("\${dataland.notification.data-ownership-request.cc}") semicolonSeparatedCcEmails: String,
) : BaseEmailBuilder(
    senderEmail = senderEmail,
    senderName = senderName,
    semicolonSeparatedReceiverEmails = semicolonSeparatedReceiverEmails,
    semicolonSeparatedCcEmails = semicolonSeparatedCcEmails,
) {
    private fun buildUserInfo(
        userAuthentication: DatalandAuthentication,
    ): String {
        return (userAuthentication as DatalandJwtAuthentication).let {
            "User ${it.username} (Keycloak ID: ${it.userId})"
        }
    }

    /**
     * Function that generates an email for a data ownership request
     */
    fun buildDataOwnershipRequest(
        companyId: String,
        userAuthentication: DatalandAuthentication,
        comment: String?,
    ): Email {
        return Email(
            senderEmailContact,
            receiverEmailContacts,
            ccEmailContacts,
            buildPropertyStyleEmailContent(
                "Dataland Data Ownership Request",
                "A data ownership request has been submitted",
                "Data Ownership Request",
                mapOf(
                    "Environment" to proxyPrimaryUrl,
                    "User" to buildUserInfo(userAuthentication),
                    "Company (Dataland ID)" to companyId,
                    "Comment" to comment,
                ),
            ),
        )
    }
}
