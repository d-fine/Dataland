package org.dataland.datalandemailservice.services.templateemail

import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Generate an email to inform about a summary of data uploads
 */
@Component
class SummaryNotificationEmailFactory(
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
        val frameworks = "frameworks"
        val baseUrl = "baseUrl"
        val duration = "duration"
    }

    override val builderForType = TemplateEmailMessage.Type.SummaryNotification

    override val requiredProperties = setOf(
        keys.companyId, keys.companyName,
        keys.frameworks, keys.baseUrl, keys.duration,
    )

    override val optionalProperties = emptySet<String>()

    override val templateFile = "/ir_engagement_data_upload_summary.html.ftl"

    override fun buildSubject(properties: Map<String, String?>): String {
        return "New data for ${properties[keys.companyName]} on Dataland"
    }

    override fun buildTextContent(properties: Map<String, String?>): String {
        return StringBuilder().apply {
            append("Exciting news! 📣\n")
            append("Multiple datasets for ${properties[keys.companyName]} have been uploaded to Dataland\n")
            append("in the last ${properties[keys.duration]}\n\n")

            append("${properties[keys.frameworks]}\n\n")

            append("How to proceed?\n")
            append("1. Gain sovereignty over your data by claiming company ownership.\n")
            append("2. Inspect, add, correct, remove data of your company.\n\n")

            append("Click the link below to claim company ownership:\n")
            append("${properties[keys.baseUrl]}/companies/${properties[keys.companyId]}\n\n")

            append("Claiming ownership process usually requires 1-2 business days.\n")
            append("You will be notified by email.\n")
        }.toString()
    }
}
