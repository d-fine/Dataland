package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.datalandemail.email.BaseEmailBuilder
import org.dataland.datalandemail.email.Email
import org.dataland.datalandemail.email.EmailContact
import org.dataland.datalandemail.email.EmailContent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * A class that manages generating emails regarding bulk data requests
 */
@Component
class SingleDataRequestEmailBuilder(
    @Value("\${dataland.notification.sender.address}") senderEmail: String,
    @Value("\${dataland.notification.sender.name}") senderName: String,
    @Autowired val companyGetter: CompanyGetter,
) : BaseEmailBuilder(
    senderEmail = senderEmail,
    senderName = senderName,
) {
    /**
     * Function that generates the email to be sent
     */
    fun buildSingleDataRequestEmail(
        requesterEmail: String,
        companyId: String?,
        singleDataRequest: SingleDataRequest,
    ): Email {
        val companyName = if (companyId != null) companyGetter.getCompanyInfo(companyId).companyName else null
        val content = EmailContent(
            subject = "A message from Dataland: Your ESG data are high on demand!",
            textContent = buildTextContent(requesterEmail, companyName, singleDataRequest),
            htmlContent = buildHtmlContent(requesterEmail, companyName, singleDataRequest),
        )
        return Email(
            sender = senderEmailContact,
            receivers = singleDataRequest.contactList!!.map { EmailContact(it) },
            cc = null,
            content = content,
        )
    }

    private fun buildTextContent(
        requesterEmail: String,
        companyName: String?,
        singleDataRequest: SingleDataRequest
    ): String {
        return StringBuilder()
            .append("You have been invited to provide data on Dataland.\n")
            .append("People are interested in ${readableFrameworkNameMapping[singleDataRequest.frameworkName]} data")
            .also {
                if (companyName != null) {
                    it.append(" for $companyName")
                }
            }
            .append(" for the year ${singleDataRequest.listOfReportingPeriods.max()}.\n")
            .also {
                if (!singleDataRequest.message.isNullOrBlank()) {
                    it.append("User $requesterEmail sent the following message:\n")
                    it.append(singleDataRequest.message)
                }
            }
            .toString()
    }

    private fun buildHtmlContent(
        requesterEmail: String,
        companyName: String?,
        singleDataRequest: SingleDataRequest
    ): String {
        // TODO extract data as for text and map to template file
        return "<html><body>${buildTextContent(requesterEmail, companyName, singleDataRequest)}</body></html>"
    }
}
