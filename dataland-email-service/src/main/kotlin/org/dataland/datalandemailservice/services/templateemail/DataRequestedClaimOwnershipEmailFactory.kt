package org.dataland.datalandemailservice.services.templateemail

import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Generate an emails when data is requested and ownership is claimed
 */
@Component
class DataRequestedClaimOwnershipEmailFactory(
    @Value("\${dataland.proxy.primary.url}") proxyPrimaryUrl: String,
    @Value("\${dataland.notification.sender.address}") senderEmail: String,
    @Value("\${dataland.notification.sender.name}") senderName: String,
) : TemplateEmailFactory(
    proxyPrimaryUrl = proxyPrimaryUrl,
    senderEmail = senderEmail,
    senderName = senderName,
) {
    private val keys = object {
        val companyId = "companyId"
        val companyName = "companyName"
        val requesterEmail = "requesterEmail"
        val firstName = "firstName"
        val lastName = "lastName"
        val dataType = "dataType"
        val reportingPeriods = "reportingPeriods"
        val message = "message"
    }

    override val builderForType = TemplateEmailMessage.Type.ClaimOwnership
    override val requiredProperties = setOf(
        keys.companyId, keys.companyName,
        keys.requesterEmail, keys.dataType, keys.reportingPeriods,
    )
    override val optionalProperties = setOf(keys.message, keys.firstName, keys.lastName)

    override val templateFile = "/claim_ownership.html.ftl"
    override fun buildSubject(properties: Map<String, String?>): String {
        return "A message from Dataland: Your ESG data are high on demand!"
    }

    override fun buildTextContent(properties: Map<String, String?>): String {
        val hasMultipleReportingPeriods = properties[keys.reportingPeriods]?.contains(",") ?: false
        return StringBuilder()
            .append("Greetings!\n\nYou have been invited to provide data on Dataland.\n")
            .append("People are interested in ${properties[keys.dataType]} data")
            .append(
                " from ${properties[keys.companyName]} for the year${if (hasMultipleReportingPeriods) "s" else ""}",
            )
            .append(" ${properties[keys.reportingPeriods]}.\n")
            .also {
                if (!properties[keys.message].isNullOrBlank()) {
                    it.append("User ${properties[keys.requesterEmail]} sent the following message:\n")
                    it.append(properties[keys.message])
                }
            }
            .append("\n\nRegister as a company owner on $proxyPrimaryUrl/companies/${properties[keys.companyId]}")
            .toString()
    }
}
