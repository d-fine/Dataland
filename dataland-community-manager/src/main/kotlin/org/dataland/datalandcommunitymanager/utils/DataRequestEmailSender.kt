package org.dataland.datalandcommunitymanager.utils

import com.fasterxml.jackson.databind.ObjectMapper
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
class DataRequestEmailSender(
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
        companyName: String,
        correlationId: String = UUID.randomUUID().toString(),
    ) {
        val properties = mapOf(
            "companyId" to dataRequestEntity.datalandCompanyId,
            "companyName" to companyName,
            "dataType" to dataRequestEntity.dataType,
            "reportingPeriods" to dataRequestEntity.reportingPeriod,
            "creationTimestamp" to getDateFromUnitTime(dataRequestEntity.creationTimestamp),
            "dataTypeName" to getDataTypeName(dataRequestEntity.dataType),
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
    private fun getDateFromUnitTime(creationTimestamp: Long):String{
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss")
        return dateFormat.format(creationTimestamp)
    }
    private fun getUserEmailById(userId :String):String{
        return "byUserId@testmail.com" //todo userId -> user mail
    }
    private fun getDataTypeName(dataType : String) :String {
        when(dataType){
            "eutaxonomy-financials" -> return "EU Taxonomy for financial companies"
            "eutaxonomy-non-financials" -> return "EU Taxonomy for non-financial companies"
            "lksg" -> return "LkSG"
            "sfdr" ->return "SFDR"
            "sme" -> return "SME"
            "p2p" -> return "WWF Pathway to Paris"
        }
        return dataType
    }
}
