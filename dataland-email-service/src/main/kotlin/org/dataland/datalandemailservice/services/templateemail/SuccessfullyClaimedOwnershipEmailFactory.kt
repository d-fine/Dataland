package org.dataland.datalandemailservice.services.templateemail

import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Generate an email to inform a user has become company owner
 */
@Component
class SuccessfullyClaimedOwnershipEmailFactory(
    @Value("\${dataland.proxy.primary.url}") proxyPrimaryUrl: String,
    @Value("\${dataland.notification.sender.address}") senderEmail: String,
    @Value("\${dataland.notification.sender.name}") senderName: String,
) : TemplateEmailFactory(
        proxyPrimaryUrl = proxyPrimaryUrl,
        senderEmail = senderEmail,
        senderName = senderName,
    ) {
    private val keys =
        object {
            val companyId = "companyId"
            val companyName = "companyName"
            val numberOfOpenDataRequestsForCompany = "numberOfOpenDataRequestsForCompany"
        }

    override val builderForType = TemplateEmailMessage.Type.SuccessfullyClaimedOwnership

    override val requiredProperties =
        setOf(
            keys.companyId, keys.companyName,
            keys.numberOfOpenDataRequestsForCompany,
        )

    override val optionalProperties = setOf<String>()
    override val templateFile = "/claimed_ownership_successfully.html.ftl"

    override fun buildSubject(properties: Map<String, String?>): String =
        "Your company ownership claim for" +
            " ${properties[keys.companyName]}" + " is confirmed!"

    override fun buildTextContent(properties: Map<String, String?>): String =
        StringBuilder()
            .append("Great news!\n")
            .append(
                "You've successfully claimed company ownership for " +
                    "${properties[keys.companyName]}\n\n",
            ).append(
                "Now, take the next step to access your company overview, view your data requests," +
                    " and provide data.",
            ).append(
                "$proxyPrimaryUrl/companies/${properties[keys.companyId]}",
            ).append(
                "Please note, that ${properties[keys.companyName]} has " +
                    "${properties[keys.numberOfOpenDataRequestsForCompany]}" +
                    " open data requests.",
            ).toString()
}
