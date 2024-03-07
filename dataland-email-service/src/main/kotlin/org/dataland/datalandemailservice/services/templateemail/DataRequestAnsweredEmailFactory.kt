package org.dataland.datalandemailservice.services.templateemail

import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Generate an emails when a data request is answered
 */
@Component
class DataRequestAnsweredEmailFactory(
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
        val dataType = "dataType"
        val reportingPeriods = "reportingPeriods"
        val creationTimestamp = "creationTimestamp"
        val closedIn = "closedIn"
        val dataTypeDescription = "dataTypeDescription"
    }

    override val builderForType = TemplateEmailMessage.Type.DataRequestedAnswered
    override val requiredProperties = setOf(
        keys.companyId, keys.companyName, keys.dataType, keys.reportingPeriods, keys.creationTimestamp,
    )
    override val optionalProperties = setOf(keys.closedIn, keys.dataTypeDescription)

    override val templateFile = "/request_answered.html.ftl"
    override val subject = "Your data request has been answered!"

    override fun buildTextContent(properties: Map<String, String?>): String {
        val closedInDays = keys.closedIn.ifEmpty { "some days" }
        return StringBuilder()
            .append(
                "Great news!\n" +
                    "Your data request has been answered.\n\n",
            )
            .append("Company: ${properties[keys.companyName]} \n")
            .append("Framework: ${properties[keys.dataType]} \n")
            .append("Reporting period(s): ${properties[keys.creationTimestamp]} \n\n")
            .append("Go to your data requests:\n")
            .append("$proxyPrimaryUrl/companies/${properties[keys.companyId]}/frameworks/${properties[keys.dataType]}")
            .append("\nWithout any actions, your data request will be set to closed automatically in $closedInDays.")
            .toString()
    }
}
