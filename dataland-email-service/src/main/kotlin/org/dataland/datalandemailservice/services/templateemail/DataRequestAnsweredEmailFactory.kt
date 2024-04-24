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
        val reportingPeriod = "reportingPeriod"
        val creationDate = "creationDate"
        val dataRequestId = "dataRequestId"
        val closedIn = "closedIn"
        val dataTypeDescription = "dataTypeDescription"
    }

    override val builderForType = TemplateEmailMessage.Type.DataRequestedAnswered
    override val requiredProperties = setOf(
        keys.companyId, keys.companyName, keys.dataType, keys.reportingPeriod, keys.creationDate,
        keys.dataRequestId,
    )
    override val optionalProperties = setOf(keys.closedIn, keys.dataTypeDescription)

    override val templateFile = "/request_answered.html.ftl"
    override fun buildSubject(properties: Map<String, String?>): String {
        return "Your data request has been answered!"
    }

    override fun buildTextContent(properties: Map<String, String?>): String {
        return StringBuilder()
            .append(
                "Great news!\n" +
                    "Your data request has been answered.\n\n",
            )
            .append("Company: ${properties[keys.companyName]} \n")
            .append("Framework: ${properties[keys.dataType]} \n")
            .append("Reporting period: ${properties[keys.reportingPeriod]} \n\n")
            .append("Request created: ${properties[keys.creationDate]} \n\n")
            .append("Review the provided data:\n")
            .append("$proxyPrimaryUrl/requests/${properties[keys.dataRequestId]}")
            .append("\nWithout any actions, your data request will be set to closed automatically in some days.")
            .toString()
    }
}
