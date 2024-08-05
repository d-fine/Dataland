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
        const val DATA_REQUEST_ID = "dataRequestId"
        const val COMPANY_NAME = "companyName"
        const val DATA_TYPE = "dataType"
        const val REPORTING_PERIOD = "reportingPeriod"
        const val CREATION_DATE = "creationDate"
    }

    override val builderForType = TemplateEmailMessage.Type.DataAccessGranted

    override val requiredProperties = setOf(
        DATA_REQUEST_ID, COMPANY_NAME, DATA_TYPE, REPORTING_PERIOD, CREATION_DATE,
    )

    override val optionalProperties = emptySet<String>()

    override val templateFile = "/data_access_granted.html.ftl"

    override fun buildSubject(properties: Map<String, String?>): String {
        return "Your Dataland Access Request has been granted!"
    }

    override fun buildTextContent(properties: Map<String, String?>): String {
        return StringBuilder()
            .append(
                "Great news!\n" +
                    "You have now access to the following dataset on Dataland..\n\n",
            )
            .append("Company: ${properties[COMPANY_NAME]} \n")
            .append("Framework: ${properties[DATA_TYPE]} \n")
            .append("Reporting period: ${properties[REPORTING_PERIOD]} \n\n")
            .append("Request created: ${properties[CREATION_DATE]} \n\n")
            .append("Review the provided data on Dataland:\n")
            .append("$proxyPrimaryUrl/requests/${properties[DATA_REQUEST_ID]}")
            // TODO remove commented out code
            // .append(
            //    "\nWithout any actions, your data request will be set to closed " +
            //            "automatically in ${properties[Keys.CLOSED_IN_DAYS]} days.",
            // )
            .toString()
    }
}
