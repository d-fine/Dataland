package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.jvm.optionals.getOrElse

/**
 * Manages all alterations of data requests
 */
@Suppress("LongParameterList")
@Service
class DataRequestAlterationManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val requestEmailManager: RequestEmailManager,
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
    @Autowired private val utils: DataRequestProcessingUtils,
    @Autowired private val companyRolesManager: CompanyRolesManager,
) {
    private val logger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

    /**
     * Method to patch a data request.
     * @param dataRequestId the id of the data request to patch
     * @param requestStatus the request status to patch
     * @param accessStatus the access status to patch
     * @param contacts the contacts to patch
     * @param message the message to patch
     * @param requestPriority the priority of the data request, which can only be set by admins and viewed by anyone.
     * @param adminComment the admin comment, which can only be set and viewed by admins.
     *
     * @return the updated data request object
     */
    @Transactional
    @Suppress("LongParameterList")
    fun patchDataRequest(
        dataRequestId: String,
        requestStatus: RequestStatus? = null,
        accessStatus: AccessStatus? = null,
        contacts: Set<String>? = null,
        message: String? = null,
        correlationId: String? = null,
        requestPriority: RequestPriority? = null,
        adminComment: String? = null,
    ): StoredDataRequest {
        val dataRequestEntity =
            dataRequestRepository.findById(dataRequestId).getOrElse {
                throw DataRequestNotFoundApiException(dataRequestId)
            }
        val filteredContacts = contacts.takeIf { !it.isNullOrEmpty() }
        val filteredMessage = message.takeIf { !it.isNullOrEmpty() }
        filteredContacts?.forEach {
            MessageEntity.validateContact(it, companyRolesManager, dataRequestEntity.datalandCompanyId)
        }
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
            this.requestEmailManager.sendSingleDataRequestEmail(dataRequestEntity, filteredContacts, filteredMessage)
            dataRequestLogger.logMessageForPatchingRequestMessage(dataRequestEntity.dataRequestId)
        }

        val newRequestPriority = requestPriority ?: dataRequestEntity.requestPriority
        if (newRequestPriority != dataRequestEntity.requestPriority) {
            anyChanges = true
            dataRequestEntity.requestPriority = newRequestPriority
            dataRequestLogger.logMessageForPatchingRequestPriority(dataRequestEntity.dataRequestId, newRequestPriority)
        }

        // don't change modification time if only the admin comment is changed
        val newAdminComment = adminComment ?: dataRequestEntity.adminComment
        if (newAdminComment != dataRequestEntity.adminComment) {
            dataRequestEntity.adminComment = newAdminComment
            dataRequestLogger.logMessageForPatchingAdminComment(dataRequestEntity.dataRequestId, newAdminComment)
        }

        // should the last modified date be changed if request priority is changed?
        if (anyChanges) dataRequestEntity.lastModifiedDate = modificationTime

        // should we always send a mail? Do we need to send a mail if the request priority changes?
        requestEmailManager.sendEmailsWhenStatusChanged(dataRequestEntity, requestStatus, accessStatus, correlationId)

        return dataRequestEntity.toStoredDataRequest()
    }

    /**
     * Method to patch open data request to answered after a dataset is uploaded
     * @param dataId the id of the uploaded dataset
     * @param correlationId dataland correlationId
     */
    @Transactional
    fun patchRequestStatusFromOpenToAnsweredByDataId(
        dataId: String,
        correlationId: String,
    ) {
        val metaData = metaDataControllerApi.getDataMetaInfo(dataId)
        val dataRequestEntities =
            dataRequestRepository.searchDataRequestEntity(
                DataRequestsFilter(
                    dataType = setOf(metaData.dataType),
                    userId = null,
                    emailAddress = null,
                    datalandCompanyId = metaData.companyId,
                    reportingPeriod = metaData.reportingPeriod,
                    requestStatus = setOf(RequestStatus.Open),
                    accessStatus = null,
                    adminComment = null,
                    requestPriority = null,
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
