package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.AccessRequestEmailSender
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestResponseEmailSender
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
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
    @Autowired private val accessRequestEmailSender: AccessRequestEmailSender,
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
    @Autowired private val utils: DataRequestProcessingUtils,
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
        val filteredContacts = contacts.takeIf { !it.isNullOrEmpty() }
        val filteredMessage = message.takeIf { !it.isNullOrEmpty() }
        filteredContacts?.forEach { MessageEntity.validateContact(it) }

        val modificationTime = Instant.now().toEpochMilli()
        var anyChanges = false

        val newRequestStatus = requestStatus ?: dataRequestEntity.requestStatus
        val newAccessStatus = accessStatus ?: dataRequestEntity.accessStatus
        if (newRequestStatus != dataRequestEntity.requestStatus || newAccessStatus != dataRequestEntity.accessStatus) {
            anyChanges = true
            utils.addNewRequestStatusToHistory(dataRequestEntity, newRequestStatus, newAccessStatus, modificationTime)
            dataRequestLogger.logMessageForPatchingRequestStatusOrAccessStatus(
                dataRequestEntity.dataRequestId, newRequestStatus, newAccessStatus,
            )
        }
        if (filteredContacts != null) {
            anyChanges = true
            utils.addMessageToMessageHistory(dataRequestEntity, filteredContacts, filteredMessage, modificationTime)
            this.sendSingleDataRequestEmail(dataRequestEntity, filteredContacts, filteredMessage)
            dataRequestLogger.logMessageForPatchingRequestMessage(dataRequestEntity.dataRequestId)
        }
        if (anyChanges) dataRequestEntity.lastModifiedDate = modificationTime

        sendEmailsWhenStatusChanged(dataRequestEntity, requestStatus, accessStatus, correlationId)

        return dataRequestEntity.toStoredDataRequest()
    }

    private fun sendEmailsWhenStatusChanged(
        dataRequestEntity: DataRequestEntity,
        requestStatus: RequestStatus?,
        accessStatus: AccessStatus?,
        correlationId: String?,
    ) {
        val correlationId = correlationId ?: UUID.randomUUID().toString()
        if (requestStatus == RequestStatus.Answered || requestStatus == RequestStatus.Closed) {
            dataRequestResponseEmailMessageSender.sendDataRequestResponseEmail(
                dataRequestEntity,
                if (requestStatus == RequestStatus.Answered) {
                    TemplateEmailMessage.Type.DataRequestedAnswered
                } else {
                    TemplateEmailMessage.Type.DataRequestClosed
                },
                correlationId,
            )
        }
        if (accessStatus == AccessStatus.Granted) {
            accessRequestEmailSender.notifyRequesterAboutGrantedRequest(
                AccessRequestEmailSender.GrantedEmailInformation(dataRequestEntity),
                correlationId,
            )
        }
        if (requestStatus == RequestStatus.Answered && accessStatus == AccessStatus.Pending) {
            accessRequestEmailSender.notifyCompanyOwnerAboutNewRequest(
                AccessRequestEmailSender.RequestEmailInformation(dataRequestEntity),
                correlationId,
            )
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
        singleDataRequestEmailMessageSender.sendSingleDataRequestExternalMessage(
            messageInformation = SingleDataRequestEmailMessageSender.MessageInformation(
                dataType = DataTypeEnum.decode(dataRequestEntity.dataType)!!,
                reportingPeriods = setOf(dataRequestEntity.reportingPeriod),
                datalandCompanyId = dataRequestEntity.datalandCompanyId,
                userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
            ),
            receiverSet = contacts,
            contactMessage = message,
            correlationId = correlationId,
        )
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
                dataTypeFilter = metaData.dataType.value, userIdFilter = "", requestStatus = RequestStatus.Open,
                accessStatus = null, reportingPeriodFilter = metaData.reportingPeriod,
                datalandCompanyIdFilter = metaData.companyId,
            ),
        )
        dataRequestEntities.forEach {
            if (it.dataType == DataTypeEnum.vsme.name && it.accessStatus != AccessStatus.Granted) {
                patchDataRequest(
                    dataRequestId = it.dataRequestId, requestStatus = RequestStatus.Answered,
                    accessStatus = AccessStatus.Pending,
                    correlationId = correlationId,
                )
            } else {
                patchDataRequest(
                    dataRequestId = it.dataRequestId, requestStatus = RequestStatus.Answered,
                    correlationId = correlationId,
                )
            }
        }
        logger.info(
            "Changed Request Status for company Id ${metaData.companyId}, " +
                "reporting period ${metaData.reportingPeriod} and framework ${metaData.dataType.name}",
        )
    }
}
