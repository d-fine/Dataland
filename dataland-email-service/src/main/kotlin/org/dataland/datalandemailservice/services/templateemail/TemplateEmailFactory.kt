package org.dataland.datalandemailservice.services.templateemail

import org.dataland.datalandemailservice.email.BaseEmailBuilder
import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.email.EmailContent
import org.dataland.datalandemailservice.email.FreeMarker
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import java.io.StringWriter

/**
 * Base factory class for internal emails
 */
abstract class TemplateEmailFactory(
    protected val proxyPrimaryUrl: String,
    senderEmail: String,
    senderName: String,
) : BaseEmailBuilder(
        senderEmail = senderEmail,
        senderName = senderName,
    ) {
    abstract val builderForType: TemplateEmailMessage.Type
    protected abstract val requiredProperties: Set<String>
    protected abstract val optionalProperties: Set<String>

    protected abstract val templateFile: String

    protected abstract fun buildSubject(properties: Map<String, String?>): String

    protected abstract fun buildTextContent(properties: Map<String, String?>): String

    /**
     * Function that generates the email to be sent
     * @param receiverEmail the single receiver email address
     * @param properties the properties required to fill the HTML template
     */
    fun buildEmail(
        receiverEmail: String,
        properties: Map<String, String?>,
    ): Email {
        validateProperties(properties)
        val content =
            EmailContent(
                subject = buildSubject(properties),
                textContent = buildTextContent(properties),
                htmlContent = buildHtmlContent(properties),
            )
        return Email(
            sender = senderEmailContact,
            receivers = listOf(EmailContact(receiverEmail)),
            cc = null,
            content = content,
        )
    }

    private fun validateProperties(properties: Map<String, String?>) {
        validateRequiredPropertiesAreProvided(properties)
        validateNoUnknownPropertiesAreProvided(properties)
    }

    private fun validateNoUnknownPropertiesAreProvided(properties: Map<String, String?>) {
        val allPossibleProperties = requiredProperties + optionalProperties
        require(allPossibleProperties.containsAll(properties.keys.toSet())) {
            throw IllegalArgumentException("Unknown property specified")
        }
    }

    private fun validateRequiredPropertiesAreProvided(properties: Map<String, String?>) {
        requiredProperties.forEach {
            require(properties.keys.contains(it)) {
                throw IllegalArgumentException("Required key \"$it\" missing in properties")
            }
            require(!properties.getValue(it).isNullOrBlank()) {
                throw IllegalArgumentException("A non-blank value is required for the key \"$it\".")
            }
        }
    }

    /**
     * Generates the HTML version of the email
     */
    private fun buildHtmlContent(properties: Map<String, String?>): String {
        val freeMarkerContext = properties + ("baseUrl" to "https://$proxyPrimaryUrl")

        val freemarkerTemplate =
            FreeMarker.configuration
                .getTemplate(templateFile)

        val writer = StringWriter()
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
        return writer.toString()
    }
}
