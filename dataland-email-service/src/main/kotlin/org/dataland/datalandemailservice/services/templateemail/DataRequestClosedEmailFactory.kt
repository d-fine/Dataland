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
) : DataRequestResponseEmailBaseFactory(
        proxyPrimaryUrl = proxyPrimaryUrl,
        senderEmail = senderEmail,
        senderName = senderName,
    ) {
    override val builderForType = TemplateEmailMessage.Type.DataRequestClosed

    override val templateFile = "/request_closed.html.ftl"

    override fun buildSubject(properties: Map<String, String?>): String = "Your data request has been closed!"

    override fun buildTextContent(properties: Map<String, String?>): String =
        StringBuilder()
            .append(
                "Your answered data request has been automatically closed " +
                    "as no action was taken within the last ${properties[Keys.CLOSED_IN_DAYS]} days.\n\n",
            ).append("Company: ${properties[Keys.COMPANY_NAME]} \n")
            .append("Framework: ${properties[Keys.DATA_TYPE]} \n")
            .append("Reporting period: ${properties[Keys.REPORTING_PERIOD]} \n\n")
            .append("Request created: ${properties[Keys.CREATION_DATE]} \n\n")
            .append("To my data request:\n")
            .append("$proxyPrimaryUrl/requests/${properties[Keys.DATA_REQUEST_ID]}")
            .toString()
}
