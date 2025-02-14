package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
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
        private val companyDataControllerApi: CompanyDataControllerApi,
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

            if (newRequestStatus != dataRequestEntity.requestStatus ||
                newAccessStatus != dataRequestEntity.accessStatus ||
                newRequestStatus == RequestStatus.NonSourceable
            ) {
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
                    requestStatus = RequestStatus.Answered,
                    accessStatus = AccessStatus.Pending,
                    correlationId = correlationId,
                    requestStatusChangeReason = requestStatusChangeReason,
                )
            } else {
                patchDataRequest(
                    dataRequestId = dataRequestEntity.dataRequestId,
                    requestStatus = RequestStatus.Answered,
                    correlationId = correlationId,
                    requestStatusChangeReason = requestStatusChangeReason,
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
