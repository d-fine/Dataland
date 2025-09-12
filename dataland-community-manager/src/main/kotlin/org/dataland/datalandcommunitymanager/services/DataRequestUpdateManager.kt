package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.SourceabilityInfo
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestUpdateUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Manages all updates of data requests
 */
@Suppress("LongParameterList")
@Service
class DataRequestUpdateManager
    @Autowired
    constructor(
        private val dataRequestRepository: DataRequestRepository,
        private val dataRequestSummaryNotificationService: DataRequestSummaryNotificationService,
        private val dataRequestLogger: DataRequestLogger,
        private val requestEmailManager: RequestEmailManager,
        private val metaDataControllerApi: MetaDataControllerApi,
        private val dataRequestUpdateUtils: DataRequestUpdateUtils,
        private val companyDataControllerApi: CompanyDataControllerApi,
    ) {
        /**
         * Method for creating the user-specific notification event for the given data request entity and patch.
         */
        private fun createNotificationEvent(
            dataRequestEntity: DataRequestEntity,
            dataRequestPatch: DataRequestPatch,
        ) {
            dataRequestSummaryNotificationService.createUserSpecificNotificationEvent(
                dataRequestEntity, dataRequestPatch.requestStatus, dataRequestEntity.notifyMeImmediately,
                dataRequestUpdateUtils.existsEarlierQaApprovalOfDatasetForDataDimension(
                    dataRequestEntity,
                ),
            )
        }

        /**
         * Method for sending an immediate notification email on request status change and creating its notification event.
         */
        private fun sendImmediateNotificationAndCreateNotificationEvent(
            dataRequestEntity: DataRequestEntity,
            dataRequestPatch: DataRequestPatch,
            correlationId: String,
        ) {
            sendImmediateNotificationOnRequestStatusChange(
                dataRequestEntity,
                dataRequestPatch,
                dataRequestUpdateUtils.existsEarlierQaApprovalOfDatasetForDataDimension(
                    dataRequestEntity,
                ),
                correlationId,
            )
            createNotificationEvent(dataRequestEntity, dataRequestPatch)
        }

        /**
         * Checks for changes of dataRequestEntity based on dataRequestPatch. If so, it stores the new version of the entity.
         */
        private fun updateDataRequestEntity(
            dataRequestEntity: DataRequestEntity,
            dataRequestPatch: DataRequestPatch,
            answeringDataId: String?,
        ): StoredDataRequest {
            val modificationTime = Instant.now().toEpochMilli()
            val anyChanges =
                listOf(
                    dataRequestUpdateUtils.updateNotifyMeImmediatelyIfRequired(dataRequestPatch, dataRequestEntity),
                    dataRequestUpdateUtils.updateRequestStatusHistoryIfRequired(
                        dataRequestPatch,
                        dataRequestEntity,
                        modificationTime,
                        answeringDataId,
                    ),
                    dataRequestUpdateUtils.updateMessageHistoryIfRequired(
                        dataRequestPatch,
                        dataRequestEntity,
                        modificationTime,
                    ),
                    dataRequestUpdateUtils.checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(
                        dataRequestPatch,
                        dataRequestEntity,
                    ),
                ).any { it }
            if (anyChanges) dataRequestEntity.lastModifiedDate = modificationTime
            dataRequestRepository.save(dataRequestEntity)
            return dataRequestEntity.toStoredDataRequest()
        }

        /**
         * Entry point for patch data requests coming from RequestController.
         */
        @Transactional
        fun processExternalPatchRequestForDataRequest(
            dataRequestId: String,
            dataRequestPatch: DataRequestPatch,
            correlationId: String,
        ) = patchDataRequest(
            dataRequestId,
            dataRequestPatch,
            correlationId,
        )

        /**
         * Method to patch a data request
         * @param dataRequestId the id of the data request to patch
         * @param dataRequestPatch the patch object containing information about the required changes
         * @return the updated data request object
         */
        @Suppress("ReturnCount")
        private fun patchDataRequest(
            dataRequestId: String,
            dataRequestPatch: DataRequestPatch,
            correlationId: String,
            answeringDataId: String? = null,
        ): StoredDataRequest {
            val dataRequestEntity =
                dataRequestRepository
                    .findByDataRequestId(dataRequestId) ?: throw DataRequestNotFoundApiException(dataRequestId)

            if (dataRequestEntity.requestStatus == RequestStatus.Withdrawn) {
                return updateDataRequestEntity(dataRequestEntity, dataRequestPatch, answeringDataId)
            }

            if (dataRequestEntity.dataType == DataTypeEnum.vsme.name) {
                requestEmailManager.sendNotificationsSpecificToAccessRequests(
                    dataRequestEntity, dataRequestPatch, correlationId,
                )
                requestEmailManager.sendEmailsWhenRequestStatusChanged(
                    dataRequestEntity, dataRequestPatch.requestStatus,
                    dataRequestPatch.requestStatusChangeReason,
                    dataRequestUpdateUtils.existsEarlierQaApprovalOfDatasetForDataDimension(
                        dataRequestEntity,
                    ),
                    correlationId,
                )
                return updateDataRequestEntity(dataRequestEntity, dataRequestPatch, answeringDataId)
            }

            if (dataRequestPatch.requestStatus == RequestStatus.Answered) {
                when (
                    Pair(dataRequestEntity.requestStatus, dataRequestEntity.notifyMeImmediately)
                ) {
                    Pair(RequestStatus.Open, true), Pair(RequestStatus.NonSourceable, true) -> {
                        sendImmediateNotificationAndCreateNotificationEvent(
                            dataRequestEntity, dataRequestPatch, correlationId,
                        )
                    }

                    Pair(RequestStatus.Open, false), Pair(RequestStatus.NonSourceable, false) -> {
                        createNotificationEvent(dataRequestEntity, dataRequestPatch)
                    }

                    else -> Unit
                }
            }

            if (dataRequestPatch.requestStatus == RequestStatus.NonSourceable) {
                if (dataRequestEntity.notifyMeImmediately) {
                    sendImmediateNotificationAndCreateNotificationEvent(
                        dataRequestEntity, dataRequestPatch, correlationId,
                    )
                } else {
                    createNotificationEvent(dataRequestEntity, dataRequestPatch)
                }
            }

            return updateDataRequestEntity(dataRequestEntity, dataRequestPatch, answeringDataId)
        }

        /**
         * If applicable, send an immediate notification email corresponding to the status change to the user.
         */
        private fun sendImmediateNotificationOnRequestStatusChange(
            dataRequestEntity: DataRequestEntity,
            dataRequestPatch: DataRequestPatch,
            earlierQaApprovedVersionOfDatasetExists: Boolean,
            correlationId: String,
        ) {
            requestEmailManager.sendEmailsWhenRequestStatusChanged(
                dataRequestEntity, dataRequestPatch.requestStatus, dataRequestPatch.requestStatusChangeReason,
                earlierQaApprovedVersionOfDatasetExists, correlationId,
            )
        }

        /**
         * Change the request status of a given data request to 'Answered'. Only applied to open or non-sourceable requests at the moment.
         */
        private fun patchRequestStatusToAnsweredByDataRequestEntity(
            dataRequestEntity: DataRequestEntity,
            answeringDataId: String,
            correlationId: String,
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
                    answeringDataId = answeringDataId,
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
                    answeringDataId = answeringDataId,
                )
            }
        }

        /**
         * Change the request status of all data requests of all subsidiaries of a given company, reporting period and framework.
         * At the moment, this is only used to patch from request status open or non-sourceable to answered.
         * @param parentCompanyId the ID of the company whose subsidiaries shall be processed
         * @param reportingPeriod the reporting period for which data requests shall be patched
         * @param dataType the framework for which data requests shall be patched
         * @param answeringDataId the ID of the dataset which triggered this method
         * @param correlationId the correlation ID of the QA event
         */
        private fun patchRequestStatusOfSubsidiaries(
            parentCompanyId: String,
            reportingPeriod: String,
            dataType: DataTypeEnum,
            answeringDataId: String,
            correlationId: String,
        ) {
            dataRequestLogger.logMessageForPatchingSubsidiariesToAnswered(
                parentCompanyId, reportingPeriod, dataType.name, correlationId,
            )
            val subsidiariesIds = companyDataControllerApi.getCompanySubsidiariesByParentId(parentCompanyId)
            if (subsidiariesIds.isEmpty()) return
            val dataRequestEntitiesForSubsidiaries =
                dataRequestRepository.searchDataRequestEntity(
                    DataRequestsFilter(
                        dataType = setOf(dataType),
                        datalandCompanyIds = subsidiariesIds.map { it.companyId }.toSet(),
                        reportingPeriods = setOf(reportingPeriod),
                        requestStatus = setOf(RequestStatus.Open, RequestStatus.NonSourceable),
                    ),
                )
            dataRequestEntitiesForSubsidiaries.forEach {
                patchRequestStatusToAnsweredByDataRequestEntity(
                    it,
                    answeringDataId,
                    correlationId,
                    "This data request was answered by a data upload to the parent company.",
                )
            }
        }

        /**
         * Method for processing data requests by users after an incoming QA approval or private data received event.
         */
        @Transactional
        fun processUserRequests(
            dataId: String,
            correlationId: String,
        ) {
            val dataMetaInformation = metaDataControllerApi.getDataMetaInfo(dataId)

            val parentRequestIds =
                patchRequestStatusToAnsweredForParentAndSubsidiaries(
                    dataMetaInformation = dataMetaInformation,
                    answeringDataId = dataId,
                    correlationId = correlationId,
                )

            processWithoutPatching(
                dataMetaInformation = dataMetaInformation,
                requestIdsToIgnore = parentRequestIds,
                correlationId = correlationId,
            )
        }

        /**
         * Method to patch the request status to answered for a list of data request entities and relevant data request entities
         * of subsidiaries. Only entities with status open or non-sourceable from a common QA-approval event are processed by the function.
         * @param dataMetaInformation the meta info of the QA-approved dataset
         * @param answeringDataId the id of the uploaded dataset
         * @return the list of request ids of the processed requests of the parent company
         */
        fun patchRequestStatusToAnsweredForParentAndSubsidiaries(
            dataMetaInformation: DataMetaInformation,
            answeringDataId: String,
            correlationId: String,
        ): List<String> {
            val parentCompanyId = dataMetaInformation.companyId
            val openOrNonSourceableDataRequestEntitiesForParent =
                dataRequestRepository.searchDataRequestEntity(
                    DataRequestsFilter(
                        dataType = setOf(dataMetaInformation.dataType),
                        datalandCompanyIds = setOf(parentCompanyId),
                        reportingPeriods = setOf(dataMetaInformation.reportingPeriod),
                        requestStatus = setOf(RequestStatus.Open, RequestStatus.NonSourceable),
                    ),
                )
            openOrNonSourceableDataRequestEntitiesForParent.forEach {
                patchRequestStatusToAnsweredByDataRequestEntity(
                    it, answeringDataId, correlationId,
                )
            }
            patchRequestStatusOfSubsidiaries(
                parentCompanyId,
                dataMetaInformation.reportingPeriod,
                dataMetaInformation.dataType,
                answeringDataId,
                correlationId,
            )
            return openOrNonSourceableDataRequestEntitiesForParent.map { it.dataRequestId }
        }

        /**
         * Method to deal with data requests for which no patching is required (i.e., answered/closed/resolved requests with QA-approval).
         */
        private fun processWithoutPatching(
            dataMetaInformation: DataMetaInformation,
            requestIdsToIgnore: List<String>,
            correlationId: String,
        ) {
            val answeredOrClosedOrResolvedDataRequestEntities =
                dataRequestRepository.searchDataRequestEntity(
                    DataRequestsFilter(
                        dataType = setOf(dataMetaInformation.dataType),
                        datalandCompanyIds = setOf(dataMetaInformation.companyId),
                        reportingPeriods = setOf(dataMetaInformation.reportingPeriod),
                        requestStatus = setOf(RequestStatus.Answered, RequestStatus.Closed, RequestStatus.Resolved),
                    ),
                )
            val requestsToProcess = answeredOrClosedOrResolvedDataRequestEntities.filter { it.dataRequestId !in requestIdsToIgnore }
            for (dataRequestEntity in requestsToProcess) {
                if (dataRequestEntity.dataType == DataTypeEnum.vsme.name) {
                    val accessStatusIsOkay =
                        listOf(
                            dataRequestEntity.accessStatus != AccessStatus.Declined,
                            dataRequestEntity.accessStatus != AccessStatus.Revoked,
                        ).all { it }
                    if (accessStatusIsOkay) {
                        requestEmailManager.sendDataUpdatedEmail(
                            dataRequestEntity,
                            correlationId,
                        )
                    }
                    continue
                }

                if (dataRequestEntity.notifyMeImmediately) {
                    requestEmailManager.sendDataUpdatedEmail(
                        dataRequestEntity,
                        correlationId,
                    )
                }

                dataRequestSummaryNotificationService.createUserSpecificNotificationEvent(
                    dataRequestEntity = dataRequestEntity,
                    immediateNotificationWasSent = dataRequestEntity.notifyMeImmediately,
                    earlierQaApprovedVersionOfDatasetExists = true,
                )
            }
        }

        /**
         * Method to patch all non-withdrawn data requests corresponding to a dataset to status non-sourceable.
         * @param sourceabilityInfo the info on the non-sourceable dataset
         */
        @Transactional
        fun patchAllNonWithdrawnRequestsToStatusNonSourceable(
            sourceabilityInfo: SourceabilityInfo,
            correlationId: String,
        ) {
            require(sourceabilityInfo.isNonSourceable) {
                "Expected information about a non-sourceable dataset but received information about a sourceable dataset. No requests " +
                    "are patched if a dataset is reported as sourceable until the dataset is uploaded."
            }

            val dataRequestEntities =
                dataRequestRepository.searchDataRequestEntity(
                    DataRequestsFilter(
                        dataType = setOf(sourceabilityInfo.dataType),
                        datalandCompanyIds = setOf(sourceabilityInfo.companyId),
                        reportingPeriods = setOf(sourceabilityInfo.reportingPeriod),
                        requestStatus = RequestStatus.entries.filter { it != RequestStatus.Withdrawn }.toSet(),
                    ),
                )

            dataRequestEntities.forEach {
                patchDataRequest(
                    dataRequestId = it.dataRequestId,
                    dataRequestPatch =
                        DataRequestPatch(
                            requestStatus = RequestStatus.NonSourceable,
                            requestStatusChangeReason = sourceabilityInfo.reason,
                        ),
                    correlationId = correlationId,
                )
            }
        }
    }
