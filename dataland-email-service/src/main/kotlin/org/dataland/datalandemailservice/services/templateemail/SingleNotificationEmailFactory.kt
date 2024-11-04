package org.dataland.datalandemailservice.services.templateemail

import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.services.EmailSubscriptionTracker
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Generate an email to inform about a data upload
 */
@Component
class SingleNotificationEmailFactory(
    @Value("\${dataland.proxy.primary.url}") proxyPrimaryUrl: String,
    @Value("\${dataland.notification.sender.address}") senderEmail: String,
    @Value("\${dataland.notification.sender.name}") senderName: String,
    @Autowired val emailSubscriptionTracker: EmailSubscriptionTracker,
) : TemplateEmailFactory(
        proxyPrimaryUrl = proxyPrimaryUrl,
        senderEmail = senderEmail,
        senderName = senderName,
    ) {
    private val keys =
        object {
            val companyId = "companyId"
            val companyName = "companyName"
            val framework = "framework"
            val year = "year"
            val baseUrl = "baseUrl"
            val subscriptionUuid = "subscriptionUuid"
        }

    override val builderForType = TemplateEmailMessage.Type.SingleNotification

    override val requiredProperties =
        setOf(
            keys.companyId, keys.companyName,
            keys.framework, keys.year, keys.baseUrl,
            keys.subscriptionUuid,
        )

    override val optionalProperties = setOf<String>()

    override val templateFile = "/ir_engagement_data_upload_single.html.ftl"

    override fun buildEmail(
        receiverEmail: String,
        properties: Map<String, String?>,
    ): Email {
        val subscriptionUuid = emailSubscriptionTracker.addSubscription(receiverEmail).toString()
        val subscriptionProperty = mapOf(keys.subscriptionUuid to subscriptionUuid)
        return super.buildEmail(receiverEmail, properties + subscriptionProperty)
    }

    override fun buildSubject(properties: Map<String, String?>): String = "New data for ${properties[keys.companyName]} on Dataland"

    override fun buildTextContent(properties: Map<String, String?>): String =
        StringBuilder()
            .apply {
                append("Exciting news! 📣\n")
                append("Data for ${properties[keys.companyName]} has been uploaded to Dataland!\n\n")

                append("Framework: ${properties[keys.framework]}\n")
                append("Reporting year: ${properties[keys.year]}\n\n")

                append("How to proceed?\n")
                append("1. Gain sovereignty over your data by claiming company ownership.\n")
                append("2. Inspect, add, correct, remove data of your company.\n\n")

                append("Click the link below to claim company ownership:\n")
                append("${properties[keys.baseUrl]}/companies/${properties[keys.companyId]}\n\n")

                append("Claiming ownership process usually requires 1-2 business days.\n")
                append("You will be notified by email.\n")
            }.toString()
}
