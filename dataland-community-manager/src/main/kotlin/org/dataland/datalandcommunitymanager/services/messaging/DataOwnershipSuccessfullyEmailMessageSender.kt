package org.dataland.datalandbackend.services.messaging

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.services.DataRequestQueryManager
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * A class that manages generating emails messages when data ownership request is accepted
 */

@Component
class DataOwnershipSuccessfullyEmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val dataRequestQueryManager: DataRequestQueryManager,
    @Qualifier("AuthenticatedOkHttpClient") val authenticatedOkHttpClient: OkHttpClient,
    @Value("\${dataland.keycloak.base-url}") private val keycloakBaseUrl: String,
) {
    /**
     * Function that generates the message object for data ownership request acceptance mails
     * @param newDataOwnerId the id of the user promoted to data owner
     * @param datalandCompanyId identifier of the company in dataland
     * @param companyName the name of the company
     * @param correlationId the correlation ID of the current user process.
     */
    fun sendDataOwnershipAcceptanceExternalEmailMessage(
        newDataOwnerId: String,
        datalandCompanyId: String,
        companyName: String,
        correlationId: String,
    ) {
        val newDataOwnerEmail = this.getEmailAddressDataOwner(newDataOwnerId)
        val properties = mapOf(
            "companyId" to datalandCompanyId,
            "companyName" to companyName,
            "numberOfOpenDataRequestsForCompany" to getNumberOfOpenDataRequestsForCompany(datalandCompanyId).toString(),
        )
        val message = TemplateEmailMessage(
            TemplateEmailMessage.Type.SuccessfullyClaimedOwnership,
            newDataOwnerEmail,
            properties,
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SendTemplateEmail,
            correlationId,
            ExchangeName.SendEmail,
            RoutingKeyNames.templateEmail,
        )
    }

    /**
     * Function that counts the number of data requests that a company has open
     * @param datalandCompanyId identifier of the company in dataland
     * @return the number of opened data requests
     */
    fun getNumberOfOpenDataRequestsForCompany(datalandCompanyId: String): Int {
        return dataRequestQueryManager.getAggregatedDataRequests(
            identifierValue = datalandCompanyId,
            dataTypes = null,
            reportingPeriod = null,
            status = RequestStatus.Open,
        ).filter { it.count > 0 }.size
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class User(
        @JsonProperty("email")
        val email: String?,

        @JsonProperty("id")
        val userId: String,
    )

    /**
     * gets the email address of a new data owner in keycloak given the user id
     * @param userIdDataOwner the userId of the new data owner
     * @returns the email address
     */
    fun getEmailAddressDataOwner(userIdDataOwner: String): String {
        val request = Request.Builder()
            .url("$keycloakBaseUrl/admin/realms/datalandsecurity/users/$userIdDataOwner")
            .build()
        val response = authenticatedOkHttpClient.newCall(request).execute()
        val parsedResponseBody = objectMapper.readValue(
            response.body!!.string(),
            User::class.java,
        )
        return parsedResponseBody.email ?: ""
    }
}
