package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackendutils.email.BaseEmailBuilder
import org.dataland.datalandbackendutils.email.Email
import org.dataland.datalandbackendutils.email.EmailContent
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * This enum contains possible causes to generate an email. This cause can then be included in the log message.
 */
enum class CauseOfMail(val description: String) {
    BulkDataRequest("bulk data request"),
    ;

    override fun toString(): String {
        return description
    }
}

/**
 * A class that manages generating emails
 */
@Component("EmailBuilder")
class EmailBuilder(
    @Value("\${dataland.proxy.primary.url}") private val propProxyPrimaryUrl: String,
    @Value("\${dataland.notification.sender.address}") senderEmail: String,
    @Value("\${dataland.notification.sender.name}") senderName: String,
    @Value("\${dataland.notification.bulk-data-request.receivers}") semicolonSeparatedReceiverEmails: String,
    @Value("\${dataland.notification.bulk-data-request.cc}") semicolonSeparatedCcEmails: String,
) : BaseEmailBuilder(
    senderEmail = senderEmail,
    senderName = senderName,
    semicolonSeparatedReceiverEmails = semicolonSeparatedReceiverEmails,
    semicolonSeparatedCcEmails = semicolonSeparatedCcEmails,
) {
    private fun buildUserInfo(): String {
        val user = DatalandAuthentication.fromContext() as DatalandJwtAuthentication
        return "User ${user.username} (Keycloak id: ${user.userId})"
    }

    private fun buildBulkDataRequestEmailText(
        bulkDataRequest: BulkDataRequest,
        acceptedCompanyIdentifiers: List<String>,
    ): String {
        return "A bulk data request has been submitted: " +
            "Environment: $propProxyPrimaryUrl " +
            "User: ${buildUserInfo()} " +
            "Requested frameworks: ${bulkDataRequest.listOfFrameworkNames.joinToString(", ")}. " +
            "Accepted company identifiers: ${acceptedCompanyIdentifiers.joinToString(", ")}."
    }

    private fun buildBulkDataRequestEmailHtml(
        bulkDataRequest: BulkDataRequest,
        acceptedCompanyIdentifiers: List<String>,
    ): String {
        return """
        <html>
        <head>
                $defaultMailStyleHtml
        </head>
        <body>
            <div class="container">
                <div class="header">Bulk Data Request</div>
                <div class="section"> <span class="bold">Environment: </span> $propProxyPrimaryUrl </div>
                <div class="section"> <span class="bold">User: </span> ${buildUserInfo()} </div>
                <div class="section"> <span class="bold">Requested Frameworks: </span> 
                    ${bulkDataRequest.listOfFrameworkNames.joinToString(", ")} </div>
                <div class="section"> <span class="bold">Accepted Company Identifiers: </span> 
                    ${acceptedCompanyIdentifiers.joinToString(", ")} </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Function that generates the email to be sent
     */
    fun buildBulkDataRequestEmail(
        bulkDataRequest: BulkDataRequest,
        acceptedCompanyIdentifiers: List<String>,
    ): Email {
        val content = EmailContent(
            "Dataland Bulk Data Request",
            buildBulkDataRequestEmailText(bulkDataRequest, acceptedCompanyIdentifiers),
            buildBulkDataRequestEmailHtml(bulkDataRequest, acceptedCompanyIdentifiers),
        )
        return Email(
            senderEmailContact,
            receiverEmailContacts,
            ccEmailContacts,
            content,
        )
    }
}
