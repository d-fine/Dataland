package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.*
/**
 * Manage sending emails to user regarding data requests
 */
@Service("DataRequestEmailSender")
class DataRequestedAnsweredEmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
) {
    /**
     * Method to informs user by mail that his request is answered.
     * @param dataRequestEntity the dataRequestEntity
     * @param companyName name of the company
     * @param correlationId correlation Id
     */
    fun sendDataRequestedAnsweredEmail(
        dataRequestEntity: DataRequestEntity,
        companyName: String = getCompanyNameById(dataRequestEntity.datalandCompanyId),
        correlationId: String = UUID.randomUUID().toString(),
    ) {
        val properties = mapOf(
            "companyId" to dataRequestEntity.datalandCompanyId,
            "companyName" to companyName,
            "dataType" to dataRequestEntity.dataType,
            "reportingPeriod" to dataRequestEntity.reportingPeriod,
            "creationDate" to convertUnitTimeInMsToDate(dataRequestEntity.creationTimestamp),
            "dataTypeDescription" to getDataTypeDescription(dataRequestEntity.dataType),
        )
        val message = TemplateEmailMessage(
            emailTemplateType = TemplateEmailMessage.Type.DataRequestedAnswered,
            receiver = getUserEmailById(dataRequestEntity.userId),
            properties = properties,
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
     * Method to retrieve companyName by companyId
     * @param companyId dataland companyId
     * @returns companyName as string
     */
    private fun getCompanyNameById(companyId: String): String {
        val companyDataControllerApi = CompanyDataControllerApi()
        return companyDataControllerApi.getCompanyInfo(companyId).companyName
    }
    /**
     * Method to convert unit time in ms to human-readable date
     * @param creationTimestamp unix time in ms
     * @returns human-readable date as string
     */
    private fun convertUnitTimeInMsToDate(creationTimestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm")
        return dateFormat.format(creationTimestamp)
    }
    /**
     * Method to retrieve userEmail by userId
     * @param userId dataland userId
     * @returns userEmail as string
     */
    private fun getUserEmailById(userId: String): String {
        return "$userId@testemail.com" // todo userId -> user mail
    }
    /**
     * Method to retrieve human-readable dataType
     * @param dataType dataland dataType
     * @returns human-readable dataType as string
     */
    private fun getDataTypeDescription(dataType: String): String {
        return when (dataType) {
            "eutaxonomy-financials" -> "EU Taxonomy for financial companies"
            "eutaxonomy-non-financials" -> "EU Taxonomy for non-financial companies"
            "lksg" -> "LkSG"
            "sfdr" -> "SFDR"
            "sme" -> "SME"
            "p2p" -> "WWF Pathway to Paris"
            "esg-questionnaire" -> "ESG Questionnaire"
            "heimathafen" -> "Heimathafen"
            else -> dataType
        }
    }
}
