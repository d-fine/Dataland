package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.isEmailAddress
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
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
        val areContactsEmails = contacts?.all { it.isEmailAddress() } ?: true
        if (!areContactsEmails) {
            throw InvalidInputApiException(
                "The contacts field should only contain email adresses.",
                "The contacts field should only contain email adresses.",
            )
        }

        val modificationTime = Instant.now().toEpochMilli()
        var anyChanges = false

        val newRequestStatus = requestStatus ?: dataRequestEntity.requestStatus
        val newAccessStatus = accessStatus ?: dataRequestEntity.accessStatus
        // TODO  check sending out notification emails to the company owner if a new accessStatus =
        //  Pending request is stored
        // TODO check sending out notification emails to the requester once accessStatus is set to Granted,
        //  maybe Revoked and Declined
        if (newRequestStatus != dataRequestEntity.requestStatus || newAccessStatus != dataRequestEntity.accessStatus) {
            anyChanges = true
            addNewRequestStatusToHistory(dataRequestEntity, newRequestStatus, newAccessStatus, modificationTime)
        }
        if (contacts != null) {
            anyChanges = true
            addNewMessageToHistory(dataRequestEntity, contacts, message, modificationTime)
            this.sendSingleDataRequestEmail(dataRequestEntity, contacts, message)
        }
        if (requestStatus == RequestStatus.Closed || requestStatus == RequestStatus.Answered) {
            sendEmailBecauseOfStatusChanged(
                dataRequestEntity, requestStatus, correlationId ?: UUID.randomUUID().toString(),
            )
        }
        if (anyChanges) dataRequestEntity.lastModifiedDate = modificationTime
        return dataRequestEntity.toStoredDataRequest()
    }

    private fun addNewMessageToHistory(
        dataRequestEntity: DataRequestEntity,
        contacts: Set<String>,
        message: String?,
        modificationTime: Long,
    ) {
        val requestMessageObject = StoredDataRequestMessageObject(contacts, message, modificationTime)
        val requestMessageEntity = MessageEntity(requestMessageObject, dataRequestEntity)

        dataRequestHistoryManager.persistMessage(requestMessageEntity)
        dataRequestEntity.addToMessageToHistory(requestMessageEntity)

        dataRequestLogger.logMessageForPatchingRequestMessage(dataRequestEntity.dataRequestId)
    }

    private fun addNewRequestStatusToHistory(
        dataRequestEntity: DataRequestEntity,
        requestStatus: RequestStatus,
        accessStatus: AccessStatus,
        modificationTime: Long,
    ) {
        val requestStatusObject = StoredDataRequestStatusObject(requestStatus, modificationTime, accessStatus)
        val requestStatusEntity = RequestStatusEntity(requestStatusObject, dataRequestEntity)

        dataRequestHistoryManager.persistRequestStatus(requestStatusEntity)
        dataRequestEntity.addToRequestStatusHistory(requestStatusEntity)

        dataRequestLogger.logMessageForPatchingRequestStatusOrAccessStatus(
            dataRequestEntity.dataRequestId, requestStatus, accessStatus,
        )
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
    // TODO check if this logic works as intended after accessStatus was added with null value
    @Transactional
    fun patchRequestStatusFromOpenToAnsweredByDataId(dataId: String, correlationId: String) {
        val metaData = metaDataControllerApi.getDataMetaInfo(dataId)
        val dataRequestEntities = dataRequestRepository.searchDataRequestEntity(
            GetDataRequestsSearchFilter(
                metaData.dataType.value, "", RequestStatus.Open, null, metaData.reportingPeriod, metaData.companyId,
            ),
        )
        dataRequestEntities.forEach {
            if (it.dataType == DataTypeEnum.vsme.name && it.accessStatus != AccessStatus.Granted) {
                patchDataRequest(
                    it.dataRequestId, RequestStatus.Answered, AccessStatus.Pending,
                    correlationId = correlationId,
                )
            } else {
                patchDataRequest(it.dataRequestId, RequestStatus.Answered, correlationId = correlationId)
            }
        }
        logger.info(
            "Changed Request Status for company Id ${metaData.companyId}, " +
                "reporting period ${metaData.reportingPeriod} and framework ${metaData.dataType.name}",
        )
    }
}
