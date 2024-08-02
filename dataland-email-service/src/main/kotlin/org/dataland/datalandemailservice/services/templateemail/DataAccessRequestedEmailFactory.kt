package org.dataland.datalandemailservice.services.templateemail

import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DataAccessRequestedEmailFactory(
    @Value("\${dataland.proxy.primary.url}") proxyPrimaryUrl: String,
    @Value("\${dataland.notification.sender.address}") senderEmail: String,
    @Value("\${dataland.notification.sender.name}") senderName: String,
) : TemplateEmailFactory(
    proxyPrimaryUrl = proxyPrimaryUrl,
    senderEmail = senderEmail,
    senderName = senderName,
) {

    override val builderForType = TemplateEmailMessage.Type.DataAccessRequested

    override val requiredProperties = setOf<String>()

    override val optionalProperties = emptySet<String>()

    override val templateFile = "/data_access_requested.html.ftl"

    override fun buildSubject(properties: Map<String, String?>): String {
        return "Access Request for you Data on Dataland"
    }

    override fun buildTextContent(properties: Map<String, String?>): String {
        return ""
    }
}
