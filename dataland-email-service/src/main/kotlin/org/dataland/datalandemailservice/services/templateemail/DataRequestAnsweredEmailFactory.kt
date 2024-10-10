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
) : DataRequestResponseEmailBaseFactory(
        proxyPrimaryUrl = proxyPrimaryUrl,
        senderEmail = senderEmail,
        senderName = senderName,
    ) {
    override val builderForType = TemplateEmailMessage.Type.DataRequestedAnswered

    override val templateFile = "/request_answered.html.ftl"

    override fun buildSubject(properties: Map<String, String?>): String = "Your data request has been answered!"

    override fun buildTextContent(properties: Map<String, String?>): String =
        StringBuilder()
            .append(
                "Great news!\n" +
                    "Your data request has been answered.\n\n",
            ).append("Company: ${properties[Keys.COMPANY_NAME]} \n")
            .append("Framework: ${properties[Keys.DATA_TYPE]} \n")
            .append("Reporting period: ${properties[Keys.REPORTING_PERIOD]} \n\n")
            .append("Request created: ${properties[Keys.CREATION_DATE]} \n\n")
            .append("Review the provided data:\n")
            .append("$proxyPrimaryUrl/requests/${properties[Keys.DATA_REQUEST_ID]}")
            .append(
                "\nWithout any actions, your data request will be set to closed " +
                    "automatically in ${properties[Keys.CLOSED_IN_DAYS]} days.",
            ).toString()
}
