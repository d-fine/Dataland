package org.dataland.datalandbackend.services

import org.dataland.datalandemail.email.BaseEmailBuilder
import org.dataland.datalandemail.email.Email
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
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
    /**
     * Function that generates an email for a data ownership request
     */
    fun buildDataOwnershipRequest(
        companyId: String,
        companyName: String,
        userAuthentication: DatalandAuthentication,
        comment: String?,
    ): Email {
        return buildPropertyStyleEmail(
            "Dataland Data Ownership Request",
            "A data ownership request has been submitted",
            "Data Ownership Request",
            mapOf(
                "Environment" to proxyPrimaryUrl,
                "User" to buildUserInfo(userAuthentication),
                "Company (Dataland ID)" to companyId,
                "Company Name" to companyName,
                "Comment" to comment,
            ),
        )
    }
}
