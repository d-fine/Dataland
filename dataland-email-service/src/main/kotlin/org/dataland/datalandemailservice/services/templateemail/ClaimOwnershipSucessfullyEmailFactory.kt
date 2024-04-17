package org.dataland.datalandemailservice.services.templateemail

import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Generate an email to inform a user has become company owner
 */
@Component
class ClaimOwnershipSucessfullyEmailFactory(
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
        val message = "message"
    }

    override val builderForType = TemplateEmailMessage.Type.ClaimedOwershipSucessfully

    override val requiredProperties = setOf(
        keys.companyId, keys.companyName,
        keys.requesterEmail,
    )

    override val optionalProperties = setOf(keys.message)
    override val templateFile = "/claimed_ownership_successfully.html.ftl"

    override val subject = "A message from Dataland: Your data ownership claim is confirmed!"

    override fun buildTextContent(properties: Map<String, String?>): String {
        return StringBuilder()
            .append("Great news!\n")
            .append("You've been successfully claimed data ownership for ${properties[keys.companyName]}\n\n")
            .append(
                "Now, take the next step to access your company overview, view your data requests, and provide data",
            )
            .also {
                if (!properties[keys.message].isNullOrBlank()) {
                    it.append("User ${properties[keys.requesterEmail]} sent the following message:\n")
                    it.append(properties[keys.message])
                }
            }
            .toString()
    }
}
