package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandemail.email.BaseEmailBuilder
import org.dataland.datalandemail.email.Email
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
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
 * A class that manages generating emails regarding bulk data requests
 */
@Component
class BulkDataRequestEmailBuilder(
    @Value("\${dataland.proxy.primary.url}") private val proxyPrimaryUrl: String,
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
    /**
     * Function that generates the email to be sent
     */
    fun buildBulkDataRequestEmail(
        bulkDataRequest: BulkDataRequest,
        acceptedCompanyIdentifiers: List<String>,
    ): Email {
        return buildPropertyStyleEmail(
            "Dataland Bulk Data Request",
            "A bulk data request has been submitted",
            "Bulk Data Request",
            mapOf(
                "Environment" to proxyPrimaryUrl,
                "User" to buildUserInfo(DatalandAuthentication.fromContext()),
                "Requested Frameworks" to bulkDataRequest.listOfFrameworkNames.joinToString(", "),
                "Accepted Company Identifiers" to acceptedCompanyIdentifiers.joinToString(", "),
            ),
        )
    }
}
