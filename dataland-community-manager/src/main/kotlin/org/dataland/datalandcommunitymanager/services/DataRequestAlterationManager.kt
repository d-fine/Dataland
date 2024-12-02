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
class DataRequestAlterationManager
    @Autowired
    constructor(
        private val dataRequestRepository: DataRequestRepository,
        private val dataRequestLogger: DataRequestLogger,
        private val requestEmailManager: RequestEmailManager,
        private val metaDataControllerApi: MetaDataControllerApi,
        private val utils: DataRequestProcessingUtils,
        private val companyRolesManager: CompanyRolesManager,
    ) {
        private val logger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

        /**
         * Method to patch a data request
         * @param dataRequestId the id of the data request to patch
         * @param requestStatus the request status to patch
         * @param accessStatus the access status to patch
         * @param contacts the contacts to patch
         * @param message the message to patch
         * @param requestPriority the priority of the data request
         * @param adminComment the admin comment of the data request
         *
         * @return the updated data request object
         */
        @Transactional
        @Suppress("kotlin:S107")
        fun patchDataRequest(
            dataRequestId: String,
            requestStatus: RequestStatus? = null,
            accessStatus: AccessStatus? = null,
            contacts: Set<String>? = null,
            message: String? = null,
            correlationId: String? = null,
            requestPriority: RequestPriority? = null,
            adminComment: String? = null,
            requestStatusChangeReason: String? = null,
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
                utils.addNewRequestStatusToHistory(
                    dataRequestEntity, newRequestStatus, newAccessStatus, requestStatusChangeReason, modificationTime,
                )
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

            if (adminComment != null && adminComment != dataRequestEntity.adminComment) {
                dataRequestEntity.adminComment = adminComment
                dataRequestLogger.logMessageForPatchingAdminComment(dataRequestEntity.dataRequestId, adminComment)
            }

            if (anyChanges) dataRequestEntity.lastModifiedDate = modificationTime

            requestEmailManager
                .sendEmailsWhenStatusChanged(dataRequestEntity, requestStatus, accessStatus, correlationId)

            return dataRequestEntity.toStoredDataRequest()
        }

        /**
         * Method to patch open or non-sourceable data request to answered after a dataset is uploaded
         * @param dataId the id of the uploaded dataset
         * @param correlationId correlationId
         */
        @Transactional
        fun patchRequestStatusFromOpenOrNonSourceableToAnsweredByDataId(
            dataId: String,
            correlationId: String,
        ) {
            val metaData = metaDataControllerApi.getDataMetaInfo(dataId)
            logger.info(
                "Changing request status for company Id ${metaData.companyId}, " +
                    "reporting period ${metaData.reportingPeriod} and framework ${metaData.dataType.name} to answered. " +
                    "Correlation ID: $correlationId",
            )
            val dataRequestEntities =
                dataRequestRepository.searchDataRequestEntity(
                    DataRequestsFilter(
                        dataType = setOf(metaData.dataType),
                        userId = null,
                        emailAddress = null,
                        datalandCompanyId = metaData.companyId,
                        reportingPeriod = metaData.reportingPeriod,
                        requestStatus = setOf(RequestStatus.Open, RequestStatus.NonSourceable),
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
        }
    }
