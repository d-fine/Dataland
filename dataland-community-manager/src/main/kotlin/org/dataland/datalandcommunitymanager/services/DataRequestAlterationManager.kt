package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
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
        private val companyDataControllerApi: CompanyDataControllerApi,
    ) {
        private val logger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

        /**
         * Updates the request status history if the request status changed
         * @param dataRequestPatch the DataRequestPatch holding the data to be changed
         * @param dataRequestEntity the old DataRequestEntity that is to be changed
         * @param modificationTime the modification time in unix epoch milliseconds
         * @return true if the request status history was updated, false otherwise
         */
        private fun updateRequestStatusHistoryIfRequired(
            dataRequestPatch: DataRequestPatch,
            dataRequestEntity: DataRequestEntity,
            modificationTime: Long,
        ): Boolean {
            val newRequestStatus = dataRequestPatch.requestStatus ?: dataRequestEntity.requestStatus
            val newAccessStatus = dataRequestPatch.accessStatus ?: dataRequestEntity.accessStatus

            if (newRequestStatus != dataRequestEntity.requestStatus ||
                newAccessStatus != dataRequestEntity.accessStatus ||
                newRequestStatus == RequestStatus.NonSourceable
            ) {
                utils.addNewRequestStatusToHistory(
                    dataRequestEntity, newRequestStatus, newAccessStatus, dataRequestPatch.requestStatusChangeReason, modificationTime,
                )
                dataRequestLogger.logMessageForPatchingRequestStatusOrAccessStatus(
                    dataRequestEntity.dataRequestId, newRequestStatus, newAccessStatus,
                )
                return true
            }
            return false
        }

        /**
         * Updates the message history if valid contacts are passed in the patch object.
         * @param dataRequestPatch the DataRequestPatch holding the data to be changed
         * @param dataRequestEntity the old DataRequestEntity that is to be changed
         * @param modificationTime the modification time in unix epoch milliseconds
         * @return true if message history was updated, false otherwise
         */
        private fun updateMessageHistoryIfRequired(
            dataRequestPatch: DataRequestPatch,
            dataRequestEntity: DataRequestEntity,
            modificationTime: Long,
        ): Boolean {
            val filteredContacts = dataRequestPatch.contacts.takeIf { !it.isNullOrEmpty() }
            val filteredMessage = dataRequestPatch.message.takeIf { !it.isNullOrEmpty() }
            filteredContacts?.forEach {
                MessageEntity.validateContact(it, companyRolesManager, dataRequestEntity.datalandCompanyId)
            }
            if (filteredContacts != null) {
                utils.addMessageToMessageHistory(dataRequestEntity, filteredContacts, filteredMessage, modificationTime)
                this.requestEmailManager.sendSingleDataRequestEmail(dataRequestEntity, filteredContacts, filteredMessage)
                dataRequestLogger.logMessageForPatchingRequestMessage(dataRequestEntity.dataRequestId)
                return true
            }
            return false
        }

        /**
         * Creates log messages if required and returns whether the request priority changed
         * @param dataRequestPatch the DataRequestPatch holding the data to be changed
         * @param dataRequestEntity the old DataRequestEntity that is to be changed
         * @return whether the request priority changed
         */
        private fun checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(
            dataRequestPatch: DataRequestPatch,
            dataRequestEntity: DataRequestEntity,
        ): Boolean {
            if (dataRequestPatch.adminComment != null && dataRequestPatch.adminComment != dataRequestEntity.adminComment) {
                dataRequestEntity.adminComment = dataRequestPatch.adminComment
                dataRequestLogger.logMessageForPatchingAdminComment(dataRequestEntity.dataRequestId, dataRequestPatch.adminComment)
            }

            val newRequestPriority = dataRequestPatch.requestPriority ?: dataRequestEntity.requestPriority
            if (newRequestPriority != dataRequestEntity.requestPriority) {
                dataRequestEntity.requestPriority = newRequestPriority
                dataRequestLogger.logMessageForPatchingRequestPriority(dataRequestEntity.dataRequestId, newRequestPriority)
                return true
            }
            return false
        }

        /**
         * Method to patch a data request
         * @param dataRequestId the id of the data request to patch
         * @param dataRequestPatch the patch object containing information about the required changes
         * @param adminComment the admin comment of the data request
         *
         * @return the updated data request object
         */
        @Transactional
        fun patchDataRequest(
            dataRequestId: String,
            dataRequestPatch: DataRequestPatch,
            correlationId: String? = null,
        ): StoredDataRequest {
            val dataRequestEntity =
                dataRequestRepository.findById(dataRequestId).getOrElse {
                    throw DataRequestNotFoundApiException(dataRequestId)
                }
            val modificationTime = Instant.now().toEpochMilli()
            var anyChanges =
                listOf(
                    updateRequestStatusHistoryIfRequired(dataRequestPatch, dataRequestEntity, modificationTime),
                    updateMessageHistoryIfRequired(dataRequestPatch, dataRequestEntity, modificationTime),
                    checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(dataRequestPatch, dataRequestEntity),
                ).any { it }

            if (anyChanges) dataRequestEntity.lastModifiedDate = modificationTime

            requestEmailManager.sendEmailsWhenStatusChanged(
                dataRequestEntity, dataRequestPatch.requestStatus, dataRequestPatch.accessStatus, correlationId,
            )

            return dataRequestEntity.toStoredDataRequest()
        }

        /**
         * Change the request status of a given data request to 'Answered'
         * @param dataRequestEntity the entity specifying the data request
         * @param correlationId the correlation ID of the QA event
         */
        private fun patchRequestStatusByDataRequestEntity(
            dataRequestEntity: DataRequestEntity,
            correlationId: String?,
            requestStatusChangeReason: String? = null,
        ) {
            if (dataRequestEntity.dataType == DataTypeEnum.vsme.name && dataRequestEntity.accessStatus != AccessStatus.Granted) {
                patchDataRequest(
                    dataRequestId = dataRequestEntity.dataRequestId,
                    dataRequestPatch =
                        DataRequestPatch(
                            requestStatus = RequestStatus.Answered,
                            accessStatus = AccessStatus.Pending,
                            requestStatusChangeReason = requestStatusChangeReason,
                        ),
                    correlationId = correlationId,
                )
            } else {
                patchDataRequest(
                    dataRequestId = dataRequestEntity.dataRequestId,
                    dataRequestPatch =
                        DataRequestPatch(
                            requestStatus = RequestStatus.Answered,
                            requestStatusChangeReason = requestStatusChangeReason,
                        ),
                    correlationId = correlationId,
                )
            }
        }

        /**
         * Change the request status of all data requests for a given company, reporting period and framework
         * @param companyId the ID of the company for which data requests shall be patched
         * @param reportingPeriod the reporting period for which data requests shall be patched
         * @param dataType the framework for which data requests shall be patched
         * @param correlationId the correlation ID of the QA event
         */
        private fun patchRequestStatusOfSingleCompany(
            companyId: String,
            reportingPeriod: String,
            dataType: DataTypeEnum,
            correlationId: String,
        ) {
            logger.info(
                "Changing request status for company Id $companyId, " +
                    "reporting period $reportingPeriod and framework ${dataType.name} to answered. " +
                    "Correlation ID: $correlationId",
            )
            val dataRequestEntities =
                dataRequestRepository.searchDataRequestEntity(
                    DataRequestsFilter(
                        dataType = setOf(dataType),
                        userId = null,
                        emailAddress = null,
                        datalandCompanyIds = setOf(companyId),
                        reportingPeriod = reportingPeriod,
                        requestStatus = setOf(RequestStatus.Open, RequestStatus.NonSourceable),
                        accessStatus = null,
                        adminComment = null,
                        requestPriority = null,
                    ),
                )
            dataRequestEntities.forEach { patchRequestStatusByDataRequestEntity(it, correlationId) }
        }

        /**
         * Change the request status of all data requests of all subsidiaries of a given company, reporting period and framework
         * @param companyId the ID of the company whose subsidiaris shall be processed
         * @param reportingPeriod the reporting period for which data requests shall be patched
         * @param dataType the framework for which data requests shall be patched
         * @param correlationId the correlation ID of the QA event
         */
        private fun patchRequestStatusOfSubsidiaries(
            companyId: String,
            reportingPeriod: String,
            dataType: DataTypeEnum,
            correlationId: String,
        ) {
            logger.info(
                "Changing request status for all subsidiaries of company with Id $companyId, " +
                    "reporting period $reportingPeriod and framework ${dataType.name} to answered. " +
                    "Correlation ID: $correlationId",
            )
            val subsidiariesIds = companyDataControllerApi.getCompanySubsidiariesByParentId(companyId)
            val dataRequestEntities =
                dataRequestRepository.searchDataRequestEntity(
                    DataRequestsFilter(
                        dataType = setOf(dataType),
                        userId = null,
                        emailAddress = null,
                        datalandCompanyIds = subsidiariesIds.map { it.companyId }.toSet(),
                        reportingPeriod = reportingPeriod,
                        requestStatus = setOf(RequestStatus.Open, RequestStatus.NonSourceable),
                        accessStatus = null,
                        adminComment = null,
                        requestPriority = null,
                    ),
                )
            dataRequestEntities.forEach {
                patchRequestStatusByDataRequestEntity(
                    it, correlationId, "This data request was answered by a data upload to the parent company.",
                )
            }
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
            patchRequestStatusOfSingleCompany(
                metaData.companyId, metaData.reportingPeriod, metaData.dataType, correlationId,
            )
            patchRequestStatusOfSubsidiaries(
                metaData.companyId, metaData.reportingPeriod, metaData.dataType, correlationId,
            )
        }
    }
