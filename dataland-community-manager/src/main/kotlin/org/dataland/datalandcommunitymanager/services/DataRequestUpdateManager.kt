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
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.dataland.datalandmessagequeueutils.messages.NonSourceableMessage
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi.DataTypesGetInfoOnDatasets
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.slf4j.LoggerFactory
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
        private val companyInfoService: CompanyInfoService,
        private val dataRequestRepository: DataRequestRepository,
        private val dataRequestSummaryNotificationService: DataRequestSummaryNotificationService,
        private val dataRequestLogger: DataRequestLogger,
        private val requestEmailManager: RequestEmailManager,
        private val metaDataControllerApi: MetaDataControllerApi,
        private val qaControllerApi: QaControllerApi,
        private val utils: DataRequestProcessingUtils,
        private val companyRolesManager: CompanyRolesManager,
        private val companyDataControllerApi: CompanyDataControllerApi,
    ) {
        private val logger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

        /**
         * Patches the email-on-update-field if necessary and returns if it was updated.
         * @param dataRequestPatch the DataRequestPatch holding the data to be changed
         * @param dataRequestEntity the old DataRequestEntity that is to be changed
         * @return true if the notifyMeImmediately was updated, false otherwise
         */
        private fun updateNotifyMeImmediatelyIfRequired(
            dataRequestPatch: DataRequestPatch,
            dataRequestEntity: DataRequestEntity,
        ): Boolean {
            // Storing flag value in a variable so it can be cast to non-nullable Boolean type by compiler.
            val patchFlag = dataRequestPatch.notifyMeImmediately
            if (patchFlag != null) {
                val flagsAreDifferent =
                    dataRequestEntity.notifyMeImmediately != patchFlag
                if (flagsAreDifferent) {
                    dataRequestEntity.notifyMeImmediately = patchFlag
                }
                return flagsAreDifferent
            } else {
                return false
            }
        }

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
            answeringDataId: String?,
        ): Boolean {
            val newRequestStatus = dataRequestPatch.requestStatus ?: dataRequestEntity.requestStatus
            val newAccessStatus = dataRequestPatch.accessStatus ?: dataRequestEntity.accessStatus

            if (newRequestStatus != dataRequestEntity.requestStatus ||
                newAccessStatus != dataRequestEntity.accessStatus ||
                newRequestStatus == RequestStatus.NonSourceable
            ) {
                utils.addNewRequestStatusToHistory(
                    dataRequestEntity,
                    newRequestStatus,
                    newAccessStatus,
                    dataRequestPatch.requestStatusChangeReason,
                    modificationTime,
                    answeringDataId,
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
                dataRequestLogger.logMessageForPatchingAdminComment(
                    dataRequestEntity.dataRequestId,
                    dataRequestPatch.adminComment,
                )
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
         * Assuming that a QA approval event was just triggered for the data corresponding to the given
         * dataRequestEntity, checks whether there was at least one other QA approval event for the corresponding
         * triple of company, framework and reporting period.
         */
        private fun earlierQaApprovedVersionExists(dataRequestEntity: DataRequestEntity): Boolean {
            val dataTypeAsDataTypesGetInfoOnDatasets =
                DataTypesGetInfoOnDatasets.entries.first {
                    it.toString() == dataRequestEntity.dataType
                }

            return qaControllerApi
                .getInfoOnDatasets(
                    dataTypes = listOf(dataTypeAsDataTypesGetInfoOnDatasets),
                    reportingPeriods = setOf(dataRequestEntity.reportingPeriod),
                    companyName = companyInfoService.checkIfCompanyIdIsValidAndReturnName(dataRequestEntity.datalandCompanyId),
                    qaStatus = QaStatus.Accepted,
                ).size > 1
        }

        /**
         * Method for sending an immediate notification email on request status change, setting
         * the notifyMeImmediately flag in the data request entity to be reset later and creating
         * the corresponding user-specific notification event.
         */
        private fun sendImmediateNotificationAndSetUpFlagResetAndCreateNotificationEvent(
            dataRequestEntity: DataRequestEntity,
            dataRequestPatch: DataRequestPatch,
            correlationId: String,
        ) {
            val notifyImmediately = dataRequestEntity.notifyMeImmediately
            sendImmediateNotificationOnRequestStatusChange(
                dataRequestEntity,
                dataRequestPatch,
                earlierQaApprovedVersionExists(dataRequestEntity),
                correlationId,
            )
            setUpResetOfImmediateNotificationFlag(dataRequestEntity, dataRequestPatch)
            dataRequestSummaryNotificationService.createUserSpecificNotificationEvent(
                dataRequestEntity, dataRequestPatch.requestStatus,
                notifyImmediately, earlierQaApprovedVersionExists(dataRequestEntity),
            )
        }

        /**
         * Method for creating the user-specific notification event for the given data request entity and patch.
         */
        private fun createNotificationEvent(
            dataRequestEntity: DataRequestEntity,
            dataRequestPatch: DataRequestPatch,
        ) {
            dataRequestSummaryNotificationService.createUserSpecificNotificationEvent(
                dataRequestEntity, dataRequestPatch.requestStatus,
                dataRequestEntity.notifyMeImmediately, earlierQaApprovedVersionExists(dataRequestEntity),
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
                earlierQaApprovedVersionExists(dataRequestEntity),
                correlationId,
            )
            dataRequestSummaryNotificationService.createUserSpecificNotificationEvent(
                dataRequestEntity, dataRequestPatch.requestStatus,
                dataRequestEntity.notifyMeImmediately, earlierQaApprovedVersionExists(dataRequestEntity),
            )
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
                    updateNotifyMeImmediatelyIfRequired(dataRequestPatch, dataRequestEntity),
                    updateRequestStatusHistoryIfRequired(
                        dataRequestPatch,
                        dataRequestEntity,
                        modificationTime,
                        answeringDataId,
                    ),
                    updateMessageHistoryIfRequired(dataRequestPatch, dataRequestEntity, modificationTime),
                    checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(dataRequestPatch, dataRequestEntity),
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
        fun patchDataRequest(
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
                    earlierQaApprovedVersionExists(dataRequestEntity), correlationId,
                )
            } else if (dataRequestPatch.requestStatus == RequestStatus.Answered) {
                val notifyImmediately = dataRequestEntity.notifyMeImmediately
                when (
                    Pair(dataRequestEntity.requestStatus, notifyImmediately)
                ) {
                    Pair(RequestStatus.Open, true) -> {
                        sendImmediateNotificationAndSetUpFlagResetAndCreateNotificationEvent(
                            dataRequestEntity, dataRequestPatch, correlationId,
                        )
                    }

                    Pair(RequestStatus.Open, false) -> {
                        createNotificationEvent(dataRequestEntity, dataRequestPatch)
                    }

                    Pair(RequestStatus.NonSourceable, true) -> {
                        sendImmediateNotificationAndCreateNotificationEvent(
                            dataRequestEntity, dataRequestPatch, correlationId,
                        )
                    }

                    Pair(RequestStatus.NonSourceable, false) -> {
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
         * Reset the immediate notification flag of the data request entity. At the moment, only used in the
         * context of patching the request from open to answered.
         */
        private fun setUpResetOfImmediateNotificationFlag(
            dataRequestEntity: DataRequestEntity,
            dataRequestPatch: DataRequestPatch,
        ) {
            dataRequestPatch.notifyMeImmediately = !dataRequestEntity.notifyMeImmediately
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
            logger.info(
                "Changing request status for all subsidiaries of company with Id $parentCompanyId, " +
                    "reporting period $reportingPeriod and framework ${dataType.name} to answered. " +
                    "Correlation ID: $correlationId",
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
         * @param nonSourceableMessage the info on the non-sourceable dataset
         * @param correlationId correlationId
         */
        @Transactional
        fun patchAllRequestsToStatusNonSourceable(
            nonSourceableMessage: NonSourceableMessage,
            correlationId: String,
        ) {
            if (!nonSourceableMessage.isNonSourceable) {
                throw IllegalArgumentException(
                    "Expected information about a non-sourceable dataset but received information " +
                        "about a sourceable dataset. No requests are patched if a dataset is reported as " +
                        "sourceable until the dataset is uploaded.",
                )
            } else {
                val dataRequestEntities =
                    dataRequestRepository.findAllByDatalandCompanyIdAndDataTypeAndReportingPeriod(
                        datalandCompanyId = nonSourceableMessage.companyId,
                        dataType = nonSourceableMessage.dataType,
                        reportingPeriod = nonSourceableMessage.reportingPeriod,
                    )

                dataRequestEntities?.forEach {
                    patchDataRequest(
                        dataRequestId = it.dataRequestId,
                        dataRequestPatch =
                            DataRequestPatch(
                                requestStatus = RequestStatus.NonSourceable,
                                requestStatusChangeReason = nonSourceableMessage.reason,
                            ),
                        correlationId = correlationId,
                    )
                }
            }
        }
    }
