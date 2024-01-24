package org.dataland.datalandbackend.services

import org.dataland.datalandbackendutils.email.BaseEmailBuilder
import org.dataland.datalandbackendutils.email.Email
import org.dataland.datalandbackendutils.email.EmailContent
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


/**
 * A class that manages generating emails
 */
@Component("EmailBuilder")
class EmailBuilder(
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
        return if (userAuthentication is DatalandJwtAuthentication) {
            "User ${userAuthentication.username} (Keycloak id: ${userAuthentication.userId})"
        } else {
            "User (Keycloak id: ${userAuthentication.userId})"
        }
    }

    private fun buildDataOwnershipRequestEmailText(
        companyId: String,
        userAuthentication: DatalandAuthentication,
    ): String {
        return "A data ownership request has been submitted: " +
            "Environment: $proxyPrimaryUrl " +
            "User: ${buildUserInfo(userAuthentication)} " +
            "Company (Dataland ID): $companyId"
    }

    private fun buildDataOwnershipRequestEmailHtml(
        companyId: String,
        userAuthentication: DatalandAuthentication,
    ): String {
        return """
        <html>
        <head>
                $defaultMailStyleHtml
        </head>
        <body>
            <div class="container">
                <div class="header">Data Ownership Request</div>
                <div class="section"> <span class="bold">Environment: </span> $proxyPrimaryUrl </div>
                <div class="section"> <span class="bold">User: </span> ${buildUserInfo(userAuthentication)} </div>
                <div class="section"> <span class="bold">Company (Dataland ID): </span> $companyId </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Function that generates an email for a data ownership request
     */
    fun buildDataOwnershipRequest(
        companyId: String,
        userAuthentication: DatalandAuthentication,
    ): Email {
        val content = EmailContent(
            "Dataland Data Ownership Request",
            buildDataOwnershipRequestEmailText(companyId, userAuthentication),
            buildDataOwnershipRequestEmailHtml(companyId, userAuthentication),
        )
        return Email(
            senderEmailContact,
            receiverEmailContacts,
            ccEmailContacts,
            content,
        )
    }
}
