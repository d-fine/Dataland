package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * A class that manages generating emails messages for bulk and single data requests
 */
@Component
class BulkDataRequestEmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Value("\${dataland.community-manager.proxy-primary-url:local-dev.dataland.com}")
    private val proxyPrimaryUrl: String,
) : DataRequestEmailMessageSenderBase() {
    /**
     * Function that generates the message object for bulk data request mails
     */
    fun sendBulkDataRequestInternalMessage(
        bulkDataRequest: BulkDataRequest,
        acceptedCompanyIdsAndNames: List<CompanyIdAndName>,
        correlationId: String,
    ) {
        val formattedCompanies = acceptedCompanyIdsAndNames.map { formatCompanyIdAndNameForInfoMail(it) }

        val properties = mapOf(
            "User" to (DatalandAuthentication.fromContext() as DatalandJwtAuthentication).userDescription,
            "E-Mail" to (DatalandAuthentication.fromContext() as DatalandJwtAuthentication).username,
            "First Name" to (DatalandAuthentication.fromContext() as DatalandJwtAuthentication).firstName,
            "Last Name" to (DatalandAuthentication.fromContext() as DatalandJwtAuthentication).lastName,
            "Reporting Periods" to formatReportingPeriods(bulkDataRequest.reportingPeriods),
            "Requested Frameworks" to bulkDataRequest.dataTypes.joinToString(", ") { it.value },
            "Accepted Companies (Dataland ID)" to formattedCompanies.joinToString(", "),
        )
        val message = InternalEmailMessage(
            "Dataland Bulk Data Request",
            "A bulk data request has been submitted",
            "Bulk Data Request",
            properties,
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SendInternalEmail,
            correlationId,
            ExchangeName.SendEmail,
            RoutingKeyNames.internalEmail,
        )
    }

    private fun formatCompanyIdAndNameForInfoMail(companyIdAndName: CompanyIdAndName): String {
        return "<a href=\"https://$proxyPrimaryUrl/companies/${companyIdAndName.companyId}\">" +
            "${companyIdAndName.companyName}</a> (${companyIdAndName.companyId})"
    }
}
