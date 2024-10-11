package org.dataland.datalandemailservice.services.templateemail

import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * This class manages the generation of the access granted emails
 */
@Component
class DataAccessGrantedEmailFactory(
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
        const val DATA_TYPE_DESCRIPTION = "dataTypeDescription"
        const val REPORTING_PERIOD = "reportingPeriod"
        const val CREATION_DATE = "creationDate"
    }

    override val builderForType = TemplateEmailMessage.Type.DataAccessGranted

    override val requiredProperties =
        setOf(
            COMPANY_ID, COMPANY_NAME, DATA_TYPE, DATA_TYPE_DESCRIPTION, REPORTING_PERIOD, CREATION_DATE,
        )

    override val optionalProperties = emptySet<String>()

    override val templateFile = "/data_access_granted.html.ftl"

    override fun buildSubject(properties: Map<String, String?>): String = "Your Dataland Access Request has been granted!"

    override fun buildTextContent(properties: Map<String, String?>): String = ""
}
