package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.KeyValueTable
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A class that manages generating emails messages for bulk and single data requests
 */
@Component
class BulkDataRequestEmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
) {
    /**
     * Function that generates the message object for bulk data request mails
     */
    fun sendBulkDataRequestInternalMessage(
        bulkDataRequest: BulkDataRequest,
        acceptedCompanyIdsAndNames: List<CompanyIdAndName>,
        correlationId: String,
    ) {
        val formattedCompanies =
            acceptedCompanyIdsAndNames
                .map {
                    Value.List(
                        Value.RelativeLink("/companies/${it.companyId}", it.companyName), Value.Text("(${it.companyId})"),
                        separator = " ",
                    )
                }.let(Value::List)

        val datalandAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication
        val keyValueTable =
            KeyValueTable(
                "Dataland Bulk Data Request",
                "A bulk data request has been submitted",
                "Bulk Data Request",
                listOf(
                    "User" to Value.Text(datalandAuthentication.userDescription),
                    "E-Mail" to Value.Text(datalandAuthentication.username),
                    "First Name" to Value.Text(datalandAuthentication.firstName),
                    "Last Name" to Value.Text(datalandAuthentication.lastName),
                    "Reporting Periods" to Value.List(bulkDataRequest.reportingPeriods.sorted().map(Value::Text)),
                    "Requested Frameworks" to Value.List(bulkDataRequest.dataTypes.map { Value.Text(it.value) }),
                    "Accepted Companies (Dataland ID)" to formattedCompanies,
                ),
            )
        val message =
            EmailMessage(keyValueTable, listOf(EmailRecipient.Internal), listOf(EmailRecipient.InternalCc), emptyList())
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SEND_EMAIL,
            correlationId,
            ExchangeName.SEND_EMAIL,
            RoutingKeyNames.EMAIL,
        )
    }
}
