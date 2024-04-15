package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestedAnsweredEmailMessageSender
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.GetDataRequestsSearchFilter
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
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
    @Autowired private val dataRequestedAnsweredEmailMessageSender: DataRequestedAnsweredEmailMessageSender,
    @Autowired private val singleDataRequestEmailMessageSender: SingleDataRequestEmailMessageSender,
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
) {
    private val logger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

    /**
     * Method to patch the status of a data request.
     * @param dataRequestId the id of the data request to patch
     * @param requestStatus the status to apply to the data request
     * @return the updated data request object
     */
    @Transactional
    fun patchDataRequestStatus(
        dataRequestId: String,
        requestStatus: RequestStatus?,
        requestMessageObject: StoredDataRequestMessageObject?,
    ): StoredDataRequest {
        val dataRequestEntity = dataRequestRepository.findById(dataRequestId).getOrElse {
            throw DataRequestNotFoundApiException(dataRequestId)
        }
        if (requestStatus != null) {
            dataRequestLogger.logMessageForPatchingRequestStatus(dataRequestEntity.dataRequestId, requestStatus)
            dataRequestEntity.requestStatus = requestStatus
        }
        if (requestMessageObject != null) {
            dataRequestLogger.logMessageForPatchingRequestMessage(dataRequestEntity.dataRequestId, requestMessageObject)
            val newMessageHistory =
                listOf(MessageEntity(requestMessageObject, dataRequestEntity)) + dataRequestEntity.messageHistory
            dataRequestEntity.messageHistory = newMessageHistory
            this.sendSingleDataRequestEmail(dataRequestEntity, requestMessageObject)
        }
        dataRequestEntity.lastModifiedDate = Instant.now().toEpochMilli()
        dataRequestRepository.save(dataRequestEntity)
        if (requestStatus == RequestStatus.Answered) {
            val correlationId = UUID.randomUUID().toString()
            dataRequestedAnsweredEmailMessageSender.sendDataRequestedAnsweredEmail(dataRequestEntity, correlationId)
        }
        return dataRequestEntity.toStoredDataRequest()
    }

    /**
     * Method to send email if the message history is updated
     * @param dataRequestEntity the id of the request entity
     * @param requestMessageObject the message object
     */
    private fun sendSingleDataRequestEmail(
        dataRequestEntity: DataRequestEntity,
        requestMessageObject: StoredDataRequestMessageObject,
    ) {
        val correlationId = UUID.randomUUID().toString()
        requestMessageObject.contacts.forEach {
            singleDataRequestEmailMessageSender.sendSingleDataRequestExternalMessage(
                messageInformation = SingleDataRequestEmailMessageSender.MessageInformation(
                    dataType = DataTypeEnum.decode(dataRequestEntity.dataType)!!,
                    reportingPeriods = setOf(dataRequestEntity.reportingPeriod),
                    datalandCompanyId = dataRequestEntity.datalandCompanyId,
                    userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
                ),
                receiver = it,
                contactMessage = requestMessageObject.message,
                correlationId = correlationId,
            )
        }
    }

    /**
     * Method to patch open data request to answered after a dataset is uploaded
     * @param dataId the id of the uploaded dataset
     * @param correlationId dataland correlationId
     */
    fun patchRequestStatusFromOpenToAnsweredByDataId(dataId: String, correlationId: String) {
        val metaData = metaDataControllerApi.getDataMetaInfo(dataId)
        val dataRequestEntities = dataRequestRepository.searchDataRequestEntity(
            GetDataRequestsSearchFilter(
                metaData.dataType.value, "", RequestStatus.Open, metaData.reportingPeriod, metaData.companyId,
            ),
        )
        dataRequestRepository.updateDataRequestEntitiesFromOpenToAnswered(
            metaData.companyId, metaData.reportingPeriod, metaData.dataType.value,
        )
        dataRequestEntities.forEach {
            dataRequestedAnsweredEmailMessageSender.sendDataRequestedAnsweredEmail(it, correlationId)
        }
        logger.info(
            "Changed Request Status for company Id ${metaData.companyId}, " +
                "reporting period ${metaData.reportingPeriod} and framework ${metaData.dataType.name}",
        )
    }
}
