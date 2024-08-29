package org.dataland.datalandemailservice.services.templateemail

import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * This class manages the generation of email for requesting access
 */
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

    /**
     * The keys component holds constants used for generation access granted emails
     */
    companion object Keys {
        const val COMPANY_ID = "companyId"
        const val COMPANY_NAME = "companyName"
        const val DATA_TYPE = "dataType"
        const val REPORTING_PERIODS = "reportingPeriods"
        const val FIRST_NAME = "firstName"
        const val LAST_NAME = "lastName"
        const val MESSAGE = "message"
        const val REQUESTER_EMAIL = "requesterEmail"
    }

    override val builderForType = TemplateEmailMessage.Type.DataAccessRequested

    override val requiredProperties =
        setOf(COMPANY_ID, COMPANY_NAME, DATA_TYPE, REPORTING_PERIODS)

    override val optionalProperties = setOf(FIRST_NAME, LAST_NAME, MESSAGE, REQUESTER_EMAIL)

    override val templateFile = "/data_access_requested.html.ftl"

    override fun buildSubject(properties: Map<String, String?>): String {
        return "Access to your data has been requested on Dataland!"
    }

    override fun buildTextContent(properties: Map<String, String?>): String {
        val hasMultipleReportingPeriods = properties[REPORTING_PERIODS]?.contains(",") ?: false

        val userStringStart = buildUserStringStart(properties)

        val messageString = if (properties[MESSAGE] != null) {
            """
            The user also send the following message: 
              ${properties[MESSAGE]}." 
            """.trimIndent()
        } else {
            ""
        }

        return """
            Great News! 
            Your data are in high demand on Dataland! 
            $userStringStart is requesting access to your data from ${properties[COMPANY_NAME]} on dataland.
            
            The user is asking for your ${properties[DATA_TYPE]} data for the year${if (hasMultipleReportingPeriods) {
            "s"
        } else {
            ""
        }}.
            You can contact the user with their Email-Address ${properties[REQUESTER_EMAIL]}.
            $messageString
            
            You can verify the access request and grant access to your data on Dataland: 
            $proxyPrimaryUrl/companies/${properties[COMPANY_ID]}.
        """.trimIndent()
    }

    private fun buildUserStringStart(properties: Map<String, String?>): String {
        val userStringStart =
            if (properties[FIRST_NAME] != null || properties[LAST_NAME] != null) {
                "The user ${properties[FIRST_NAME] ?: ""} ${properties[LAST_NAME] ?: ""}"
            } else {
                "A user"
            }
        return userStringStart
    }
}
