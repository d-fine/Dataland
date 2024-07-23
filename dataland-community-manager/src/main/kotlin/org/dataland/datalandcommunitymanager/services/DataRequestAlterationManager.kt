package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestStatusObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestResponseEmailSender
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.GetDataRequestsSearchFilter
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
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
    @Autowired private val dataRequestResponseEmailMessageSender: DataRequestResponseEmailSender,
    @Autowired private val singleDataRequestEmailMessageSender: SingleDataRequestEmailMessageSender,
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
    @Autowired private val dataRequestHistoryManager: DataRequestHistoryManager,
) {
    private val logger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

    /**
     * Method to patch the status of a data request.
     * @param dataRequestId the id of the data request to patch
     * @param requestStatus the status to apply to the data request
     * @return the updated data request object
     */
    @Transactional
    fun patchDataRequest(
        dataRequestId: String,
        requestStatus: RequestStatus? = null,
        accessStatus: AccessStatus? = null,
        contacts: Set<String>? = null,
        message: String? = null,
        correlationId: String? = null,
    ): StoredDataRequest {
        val dataRequestEntity = dataRequestRepository.findById(dataRequestId).getOrElse {
            throw DataRequestNotFoundApiException(dataRequestId)
        }
        val modificationTime = Instant.now().toEpochMilli()
        dataRequestEntity.lastModifiedDate = modificationTime
        dataRequestRepository.save(dataRequestEntity)

        val newRequestStatus = requestStatus ?: dataRequestEntity.requestStatus
        val newAccessStatus = accessStatus ?: dataRequestEntity.accessStatus
        // Maybe only allow accessStatus if requestStatus is answered
        if (newRequestStatus != dataRequestEntity.requestStatus || newAccessStatus != dataRequestEntity.accessStatus) {
            val requestStatusObject = listOf(
                StoredDataRequestStatusObject(newRequestStatus, modificationTime, newAccessStatus),
            )

            dataRequestEntity.associateRequestStatus(requestStatusObject)
            dataRequestHistoryManager.saveStatusHistory(dataRequestEntity.dataRequestStatusHistory)
            dataRequestLogger.logMessageForPatchingRequestStatusOrAccessStatus(
                dataRequestId, newRequestStatus, newAccessStatus,
            )
            if (contacts != null) {
                dataRequestHistoryManager.detachDataRequestEntity(dataRequestEntity)
            }
        }
        if (contacts != null) {
            val messageHistory = listOf(StoredDataRequestMessageObject(contacts, message, modificationTime))
            dataRequestEntity.associateMessages(messageHistory)
            dataRequestHistoryManager.saveMessageHistory(dataRequestEntity.messageHistory)
            this.sendSingleDataRequestEmail(dataRequestEntity, contacts, message)
            dataRequestLogger.logMessageForPatchingRequestMessage(dataRequestId)
        }
        if (requestStatus == RequestStatus.Closed || requestStatus == RequestStatus.Answered) {
            sendEmailBecauseOfStatusChanged(
                dataRequestEntity, requestStatus, correlationId ?: UUID.randomUUID().toString(),
            )
        }
        return dataRequestEntity.toStoredDataRequest()
    }

    /**
     * Method to send email if the status changed
     * @param dataRequestEntity the id of the request entity
     * @param status the patched request status
     */
    private fun sendEmailBecauseOfStatusChanged(
        dataRequestEntity: DataRequestEntity,
        status: RequestStatus,
        correlationId: String,
    ) {
        when (status) {
            RequestStatus.Answered -> {
                dataRequestResponseEmailMessageSender.sendDataRequestResponseEmail(
                    dataRequestEntity, TemplateEmailMessage.Type.DataRequestedAnswered, correlationId,
                )
            }
            RequestStatus.Closed -> {
                dataRequestResponseEmailMessageSender.sendDataRequestResponseEmail(
                    dataRequestEntity, TemplateEmailMessage.Type.DataRequestClosed, correlationId,
                )
            }
            else -> {
                throw IllegalArgumentException("Unable to send email. Unexpected status provided: $status")
            }
        }
    }

    /**
     * Method to send email if the message history is updated
     * @param dataRequestEntity the id of the request entity
     * @param contacts set of email addresses
     * @param message string content of the email
     */
    private fun sendSingleDataRequestEmail(
        dataRequestEntity: DataRequestEntity,
        contacts: Set<String>,
        message: String?,
    ) {
        val correlationId = UUID.randomUUID().toString()
        contacts.forEach {
            singleDataRequestEmailMessageSender.sendSingleDataRequestExternalMessage(
                messageInformation = SingleDataRequestEmailMessageSender.MessageInformation(
                    dataType = DataTypeEnum.decode(dataRequestEntity.dataType)!!,
                    reportingPeriods = setOf(dataRequestEntity.reportingPeriod),
                    datalandCompanyId = dataRequestEntity.datalandCompanyId,
                    userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
                ),
                receiver = it,
                contactMessage = message,
                correlationId = correlationId,
            )
        }
    }

    /**
     * Method to patch open data request to answered after a dataset is uploaded
     * @param dataId the id of the uploaded dataset
     * @param correlationId dataland correlationId
     */
    @Transactional
    fun patchRequestStatusFromOpenToAnsweredByDataId(dataId: String, correlationId: String) {
        val metaData = metaDataControllerApi.getDataMetaInfo(dataId)
        val dataRequestEntities = dataRequestRepository.searchDataRequestEntity(
            GetDataRequestsSearchFilter(
                metaData.dataType.value, "", RequestStatus.Open, metaData.reportingPeriod, metaData.companyId,
            ),
        )
        dataRequestEntities.forEach {
            patchDataRequest(it.dataRequestId, RequestStatus.Answered, correlationId = correlationId)
        }
        logger.info(
            "Changed Request Status for company Id ${metaData.companyId}, " +
                "reporting period ${metaData.reportingPeriod} and framework ${metaData.dataType.name}",
        )
    }
    // TODO need to add logic to create a AccessRequest.Pending to the request if it was uploaded for a private
//  framework
    // TODO and there was no access granted before
}
