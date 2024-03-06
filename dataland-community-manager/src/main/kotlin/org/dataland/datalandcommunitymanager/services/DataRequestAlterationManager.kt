package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrElse

/**
 * Manages all alterations of data requests
 */
@Service
class DataRequestAlterationManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Autowired private val objectMapper: ObjectMapper,
) {
    /**
     * Method to patch the status of a data request.
     * @param dataRequestId the id of the data request to patch
     * @param requestStatus the status to apply to the data request
     * @return the updated data request object
     */
    @Transactional
    fun patchDataRequestStatus(
        dataRequestId: String,
        requestStatus: RequestStatus
    ): StoredDataRequest {
        val dataRequestEntity = dataRequestRepository.findById(dataRequestId).getOrElse {
            throw DataRequestNotFoundApiException(dataRequestId)
        }
        dataRequestLogger.logMessageForPatchingRequestStatus(dataRequestEntity.dataRequestId, requestStatus)
        dataRequestEntity.requestStatus = requestStatus
        dataRequestEntity.lastModifiedDate = Instant.now().toEpochMilli()
        dataRequestRepository.save(dataRequestEntity)
        if(requestStatus == RequestStatus.Answered){
            val companyName = companyDataControllerApi.getCompanyInfo(dataRequestEntity.datalandCompanyId).companyName
            val properties = mapOf(
                "companyId" to dataRequestEntity.datalandCompanyId,
                "companyName" to companyName,
                "dataType" to dataRequestEntity.dataType,
                "reportingPeriods" to dataRequestEntity.reportingPeriod,
                "creationTimestamp" to Date(dataRequestEntity.creationTimestamp).toString(),
            )
            // todo userId to email
            val message = TemplateEmailMessage(
                emailTemplateType = TemplateEmailMessage.Type.DataRequestedAnswered,
                receiver = "johannes.haerkoetter@d-fine.com",
                properties = properties,
            )
            val correlationId = "0" //todo welche correlationId?
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                objectMapper.writeValueAsString(message),
                MessageType.SendTemplateEmail,
                correlationId,
                ExchangeName.SendEmail,
                RoutingKeyNames.templateEmail,
            )
        }
        return dataRequestEntity.toStoredDataRequest()
    }
}
