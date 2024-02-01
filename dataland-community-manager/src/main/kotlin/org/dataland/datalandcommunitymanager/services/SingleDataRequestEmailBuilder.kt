package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.email.FreeMarker
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.datalandemail.email.BaseEmailBuilder
import org.dataland.datalandemail.email.Email
import org.dataland.datalandemail.email.EmailContact
import org.dataland.datalandemail.email.EmailContent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.StringWriter

/**
 * A class that manages generating emails regarding bulk data requests
 */
@Component
class SingleDataRequestEmailBuilder(
    @Value("\${dataland.proxy.primary.url}") private val proxyPrimaryUrl: String,
    @Value("\${dataland.notification.sender.address}") senderEmail: String,
    @Value("\${dataland.notification.sender.name}") senderName: String,
    @Autowired private val companyGetter: CompanyGetter,
) : BaseEmailBuilder(
    senderEmail = senderEmail,
    senderName = senderName,
) {
    /**
     * Function that generates the email to be sent
     */
    fun buildSingleDataRequestEmail(
        requesterEmail: String,
        receiverEmail: String,
        companyId: String,
        dataType: DataTypeEnum,
        reportingPeriods: List<String>,
        message: String?,
    ): Email {
        val companyName = companyGetter.getCompanyInfo(companyId).companyName
        val content = EmailContent(
            subject = "A message from Dataland: Your ESG data are high on demand!",
            textContent = buildTextContent(requesterEmail, companyId, companyName, dataType, reportingPeriods, message),
            htmlContent = buildHtmlContent(requesterEmail, companyId, companyName, dataType, reportingPeriods, message),
        )
        return Email(
            sender = senderEmailContact,
            receivers = listOf(EmailContact(receiverEmail)),
            cc = null,
            content = content,
        )
    }

    private fun buildTextContent(
        requesterEmail: String,
        companyId: String,
        companyName: String,
        dataType: DataTypeEnum,
        reportingPeriods: List<String>,
        message: String?,
    ): String {
        return StringBuilder()
            .append("Greetings!\n\nYou have been invited to provide data on Dataland.\n")
            .append("People are interested in ${readableFrameworkNameMapping[dataType]} data")
            .append(" from $companyName  for the year${if (reportingPeriods.size > 1) "s" else ""}")
            .append(" ${reportingPeriods.joinToString(", ")}.\n")
            .also {
                if (!message.isNullOrBlank()) {
                    it.append("User $requesterEmail sent the following message:\n")
                    it.append(message)
                }
            }
            .append("\n\nRegister as a data owner under $proxyPrimaryUrl/companies/$companyId")
            .toString()
    }

    private fun buildHtmlContent(
        requesterEmail: String,
        companyId: String,
        companyName: String,
        dataType: DataTypeEnum,
        reportingPeriods: List<String>,
        message: String?,
    ): String {
        val freemarkerContext = mapOf(
            "companyId" to companyId,
            "companyName" to companyName,
            "requesterEmail" to requesterEmail,
            "message" to message,
            "dataType" to readableFrameworkNameMapping[dataType],
            "reportingPeriods" to reportingPeriods.joinToString(", "),
            "baseUrl" to proxyPrimaryUrl,
        )
        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/claim_ownership.html.ftl")

        val writer = StringWriter()
        freemarkerTemplate.process(freemarkerContext, writer)
        writer.close()
        return writer.toString()
    }
}
