package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.NonSourceableInfo
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestUpdateUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import kotlin.jvm.optionals.getOrElse

/**
 * Manages all alterations of data requests
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
        private val processingUtils: DataRequestProcessingUtils,
        private val updateUtils: DataRequestUpdateUtils,
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
                processingUtils.earlierQaApprovedVersionExists(dataRequestEntity),
            )
        }

        /**
         * Method for sending an immediate notification email on request status change and creating the corresponding
         * user-specific notification event.
         */
        private fun sendImmediateNotificationAndCreateNotificationEvent(
            dataRequestEntity: DataRequestEntity,
            dataRequestPatch: DataRequestPatch,
            correlationId: String,
        ) {
            sendImmediateNotificationOnRequestStatusChange(
                dataRequestEntity,
                dataRequestPatch,
                processingUtils.earlierQaApprovedVersionExists(dataRequestEntity),
                correlationId,
            )
            createNotificationEvent(dataRequestEntity, dataRequestPatch)
        }

        /**
         * Checks whether processing the given data request entity and patch resulted
         * in any changes on the entity; if so, it stores the new version of the entity.
         */
        private fun checkForChangesAndSaveDataRequestEntityIfSo(
            dataRequestEntity: DataRequestEntity,
            dataRequestPatch: DataRequestPatch,
            answeringDataId: String?,
        ) {
            val modificationTime = Instant.now().toEpochMilli()
            val anyChanges =
                listOf(
                    updateUtils.updateNotifyMeImmediatelyIfRequired(dataRequestPatch, dataRequestEntity),
                    updateUtils.updateRequestStatusHistoryIfRequired(
                        dataRequestPatch,
                        dataRequestEntity,
                        modificationTime,
                        answeringDataId,
                    ),
                    updateUtils.updateMessageHistoryIfRequired(dataRequestPatch, dataRequestEntity, modificationTime),
                    updateUtils.checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(dataRequestPatch, dataRequestEntity),
                ).any { it }
            if (anyChanges) dataRequestEntity.lastModifiedDate = modificationTime
            dataRequestRepository.save(dataRequestEntity)
        }

        /**
         * Entry point for patch data requests coming from RequestController.
         */
        @Transactional
        fun handlePatchDataApiRequest(
            dataRequestId: UUID,
            dataRequestPatch: DataRequestPatch,
        ) = patchDataRequest(
            dataRequestId.toString(),
            dataRequestPatch,
            UUID.randomUUID().toString(),
        )

        /**
         * Method to patch a data request
         * @param dataRequestId the id of the data request to patch
         * @param dataRequestPatch the patch object containing information about the required changes
         *
         * @return the updated data request object
         */
        private fun patchDataRequest(
            dataRequestId: String,
            dataRequestPatch: DataRequestPatch,
            correlationId: String,
            answeringDataId: String? = null,
        ): StoredDataRequest {
            val dataRequestEntity =
                dataRequestRepository
                    .findById(dataRequestId)
                    .getOrElse { throw DataRequestNotFoundApiException(dataRequestId) }

            if (dataRequestEntity.requestStatus == RequestStatus.Withdrawn) return dataRequestEntity.toStoredDataRequest()

            if (dataRequestEntity.dataType == DataTypeEnum.vsme.name) {
                requestEmailManager.sendNotificationsSpecificToAccessRequests(
                    dataRequestEntity, dataRequestPatch, correlationId,
                )
                requestEmailManager.sendEmailsWhenRequestStatusChanged(
                    dataRequestEntity, dataRequestPatch.requestStatus,
                    processingUtils.earlierQaApprovedVersionExists(dataRequestEntity), correlationId,
                )
            } else if (dataRequestPatch.requestStatus == RequestStatus.Answered) {
                val notifyImmediately = dataRequestEntity.notifyMeImmediately
                when (
                    Pair(dataRequestEntity.requestStatus, notifyImmediately)
                ) {
                    Pair(RequestStatus.Open, true), Pair(RequestStatus.NonSourceable, true) -> {
                        sendImmediateNotificationAndCreateNotificationEvent(
                            dataRequestEntity, dataRequestPatch, correlationId,
                        )
                    }

                    Pair(RequestStatus.Open, false), Pair(RequestStatus.NonSourceable, false) -> {
                        createNotificationEvent(dataRequestEntity, dataRequestPatch)
                    }

                    else -> {}
                }
            }

            checkForChangesAndSaveDataRequestEntityIfSo(dataRequestEntity, dataRequestPatch, answeringDataId)

            return dataRequestEntity.toStoredDataRequest()
        }

        /**
         * If applicable, send an immediate notification email corresponding to the status change to the user.
         * @returns whether an immediate notification was sent
         */
        private fun sendImmediateNotificationOnRequestStatusChange(
            dataRequestEntity: DataRequestEntity,
            dataRequestPatch: DataRequestPatch,
            earlierQaApprovedVersionExists: Boolean,
            correlationId: String,
        ) {
            requestEmailManager.sendEmailsWhenRequestStatusChanged(
                dataRequestEntity, dataRequestPatch.requestStatus, earlierQaApprovedVersionExists, correlationId,
            )
        }

        /**
         * Change the request status of a given data request to 'Answered'. At the moment, this is
         * only used to patch the status from open or non-sourceable to answered.
         * @param dataRequestEntity the entity specifying the data request
         * @param correlationId the correlation ID of the QA event
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
                            notifyMeImmediately =
                                if (dataRequestEntity.requestStatus == RequestStatus.Open) false else null,
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
         * Method for processing data requests by users after an incoming QA approval or private
         * data received event.
         */
        @Transactional
        fun processUserRequests(
            dataId: String,
            correlationId: String,
        ) {
            val metaData = metaDataControllerApi.getDataMetaInfo(dataId)
            val nonWithdrawnDataRequestEntities =
                dataRequestRepository.searchDataRequestEntity(
                    DataRequestsFilter(
                        dataType = setOf(metaData.dataType),
                        datalandCompanyIds = setOf(metaData.companyId),
                        reportingPeriod = metaData.reportingPeriod,
                        requestStatus =
                            setOf(
                                RequestStatus.Answered, RequestStatus.Closed, RequestStatus.NonSourceable,
                                RequestStatus.Open, RequestStatus.Resolved,
                            ),
                    ),
                )
            val openOrNonSourceableDataRequestEntities =
                nonWithdrawnDataRequestEntities.filter {
                    it.requestStatus == RequestStatus.Open || it.requestStatus == RequestStatus.NonSourceable
                }
            val answeredOrClosedOrResolvedDataRequestEntities =
                nonWithdrawnDataRequestEntities.filter {
                    it.requestStatus == RequestStatus.Answered ||
                        it.requestStatus == RequestStatus.Closed ||
                        it.requestStatus == RequestStatus.Resolved
                }

            patchRequestStatusToAnsweredForParentAndSubsidiaries(
                dataRequestEntities = openOrNonSourceableDataRequestEntities,
                answeringDataId = dataId,
                correlationId = correlationId,
            )

            processWithoutPatching(
                dataRequestEntities = answeredOrClosedOrResolvedDataRequestEntities,
                correlationId = correlationId,
            )
        }

        /**
         * Method to patch the request status to answered for a list of data request entities and relevant
         * data request entities of subsidiaries. At the moment, only entities with status open or non-sourceable
         * are treated by this function. Moreover, the entities stem from a common QA approval event, whence they
         * share the triple of company, framework and reporting period.
         * @param dataRequestEntities the entities of the parent company to process
         * @param answeringDataId the id of the uploaded dataset
         * @param correlationId correlationId
         */
        fun patchRequestStatusToAnsweredForParentAndSubsidiaries(
            dataRequestEntities: List<DataRequestEntity>,
            answeringDataId: String,
            correlationId: String,
        ) {
            if (dataRequestEntities.isEmpty()) return
            dataRequestEntities.forEach {
                patchRequestStatusToAnsweredByDataRequestEntity(
                    it, answeringDataId, correlationId,
                )
            }
            // All dataRequestEntities share their companyId, so we only need to take the first.
            val firstDataRequestEntityForParent = dataRequestEntities.first()
            val dataTypeAsEnum =
                DataTypeEnum.decode(firstDataRequestEntityForParent.dataType)
                    ?: throw IllegalArgumentException("Unable to parse data type.")
            patchRequestStatusOfSubsidiaries(
                firstDataRequestEntityForParent.datalandCompanyId,
                firstDataRequestEntityForParent.reportingPeriod,
                dataTypeAsEnum,
                answeringDataId,
                correlationId,
            )
        }

        /**
         * Method to deal with data requests for which no patching is required. At the moment, those are answered,
         * closed or resolved requests for which some new data was QA-approved.
         *
         * @param dataRequestEntities Correspond to the requests to process.
         * @param correlationId The correlation id of the QA approval event.
         */
        private fun processWithoutPatching(
            dataRequestEntities: List<DataRequestEntity>,
            correlationId: String,
        ) {
            for (dataRequestEntity in dataRequestEntities) {
                val accessStatusIsOkay =
                    dataRequestEntity.accessStatus !=
                        AccessStatus.Declined &&
                        dataRequestEntity.accessStatus != AccessStatus.Revoked
                if (dataRequestEntity.notifyMeImmediately ||
                    (dataRequestEntity.dataType == DataTypeEnum.vsme.name && accessStatusIsOkay)
                ) {
                    requestEmailManager.sendDataUpdatedEmail(
                        dataRequestEntity,
                        correlationId,
                    )
                }
                if (dataRequestEntity.dataType != DataTypeEnum.vsme.name) {
                    dataRequestSummaryNotificationService.createUserSpecificNotificationEvent(
                        dataRequestEntity = dataRequestEntity,
                        immediateNotificationWasSent = dataRequestEntity.notifyMeImmediately,
                        earlierQaApprovedVersionExists = true,
                    )
                }
            }
        }

        /**
         * Method to patch all data requests corresponding to a dataset to status non-sourceable.
         * @param nonSourceableInfo the info on the non-sourceable dataset
         * @param correlationId correlationId
         */
        @Transactional
        fun patchAllRequestsToStatusNonSourceable(
            nonSourceableInfo: NonSourceableInfo,
            correlationId: String,
        ) {
            if (!nonSourceableInfo.isNonSourceable) {
                throw IllegalArgumentException(
                    "Expected information about a non-sourceable dataset but received information " +
                        "about a sourceable dataset. No requests are patched if a dataset is reported as " +
                        "sourceable until the dataset is uploaded.",
                )
            }

            val dataRequestEntities =
                dataRequestRepository.findAllByDatalandCompanyIdAndDataTypeAndReportingPeriod(
                    datalandCompanyId = nonSourceableInfo.companyId,
                    dataType = nonSourceableInfo.dataType.toString(),
                    reportingPeriod = nonSourceableInfo.reportingPeriod,
                )

            dataRequestEntities?.forEach {
                patchDataRequest(
                    dataRequestId = it.dataRequestId,
                    dataRequestPatch =
                        DataRequestPatch(
                            requestStatus = RequestStatus.NonSourceable,
                            requestStatusChangeReason = nonSourceableInfo.reason,
                        ),
                    correlationId = correlationId,
                )
            }
        }
    }
