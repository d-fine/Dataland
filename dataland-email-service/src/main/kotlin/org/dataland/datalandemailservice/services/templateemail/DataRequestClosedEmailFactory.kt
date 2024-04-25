package org.dataland.datalandemailservice.services.templateemail

import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Generate an emails when a data request is answered
 */
@Component
class DataRequestClosedEmailFactory(
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
        val closedInDays = "closedInDays"
        val dataTypeDescription = "dataTypeDescription"
    }

    override val builderForType = TemplateEmailMessage.Type.DataRequestClosed
    override val requiredProperties = setOf(
        keys.companyId, keys.companyName, keys.dataType, keys.reportingPeriod, keys.creationDate,
        keys.dataRequestId, keys.closedInDays,
    )
    override val optionalProperties = setOf(keys.dataTypeDescription)

    override val templateFile = "/request_closed.html.ftl"
    override val subject = "Your data request has been closed!"
    // todo change to function after other email implementation story is merged

    override fun buildTextContent(properties: Map<String, String?>): String {
        return StringBuilder()
            .append(
                "Your answered data request has been automatically closed " +
                    "as no action was taken within the last ${properties[keys.closedInDays]}days.\n\n",
            )
            .append("Company: ${properties[keys.companyName]} \n")
            .append("Framework: ${properties[keys.dataType]} \n")
            .append("Reporting period: ${properties[keys.reportingPeriod]} \n\n")
            .append("Request last modified: ${properties[keys.creationDate]} \n\n")
            .append("To my data request:\n")
            .append("$proxyPrimaryUrl/requests/${properties[keys.dataRequestId]}")
            .toString()
    }
}
