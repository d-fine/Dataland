package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.NonSourceableInfo
import org.dataland.datalandbackend.openApiClient.model.QaStatus
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
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi.DataTypesGetInfoOnDatasets
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
class DataRequestUpdateManager
    @Autowired
    constructor(
        private val companyInfoService: CompanyInfoService,
        private val dataRequestRepository: DataRequestRepository,
        private val notificationService: NotificationService,
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
         * Patches the email-on-update-field if necessary anr returns if it was updated.
         * @param dataRequestPatch the DataRequestPatch holding the data to be changed
         * @param dataRequestEntity the old DataRequestEntity that is to be changed
         * @return true if the emailOnUpdate was updated, false otherwise
         */
        private fun updateEmailOnUpdateIfRequired(
            dataRequestPatch: DataRequestPatch,
            dataRequestEntity: DataRequestEntity,
        ): Boolean {
            if (dataRequestPatch.emailOnUpdate != null && dataRequestEntity.emailOnUpdate != dataRequestPatch.emailOnUpdate) {
                dataRequestEntity.emailOnUpdate = dataRequestPatch.emailOnUpdate
                return true
            }
            return false
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
         * Method to patch a data request
         * @param dataRequestId the id of the data request to patch
         * @param dataRequestPatch the patch object containing information about the required changes
         *
         * @return the updated data request object
         */
        @Transactional
        @Suppress("LongMethod")
        fun patchDataRequest(
            dataRequestId: String,
            dataRequestPatch: DataRequestPatch,
            correlationId: String,
            answeringDataId: String? = null,
        ): StoredDataRequest {
            val dataRequestEntity =
                dataRequestRepository.findById(dataRequestId).getOrElse { throw DataRequestNotFoundApiException(dataRequestId) }

            if (dataRequestEntity.requestStatus != RequestStatus.Withdrawn) {
                if (dataRequestEntity.dataType == DataTypeEnum.vsme.name &&
                    (
                        dataRequestPatch.requestStatus == RequestStatus.Open ||
                            dataRequestPatch.accessStatus == AccessStatus.Granted
                    )
                ) {
                    requestEmailManager.sendNotificationsSpecificToAccessRequests(
                        dataRequestEntity, dataRequestPatch, correlationId,
                    )
                }

                var dataTypeAsDataTypesGetInfoOnDatasets = DataTypesGetInfoOnDatasets.p2p
                for (value in DataTypesGetInfoOnDatasets.values()) {
                    if (value.toString() == dataRequestEntity.dataType) {
                        dataTypeAsDataTypesGetInfoOnDatasets = value
                    }
                }

                val earlierQaApprovedVersionExists =
                    qaControllerApi
                        .getInfoOnDatasets(
                            dataTypes = listOf(dataTypeAsDataTypesGetInfoOnDatasets),
                            reportingPeriods = setOf(dataRequestEntity.reportingPeriod),
                            companyName = companyInfoService.checkIfCompanyIdIsValidAndReturnName(dataRequestEntity.datalandCompanyId),
                            qaStatus = QaStatus.Accepted,
                        ).size > 1

                if (dataRequestPatch.requestStatus == RequestStatus.Answered ||
                    dataRequestPatch.requestStatus == RequestStatus.NonSourceable
                ) {
                    if (dataRequestEntity.dataType == DataTypeEnum.vsme.name) {
                        requestEmailManager.sendEmailsWhenRequestStatusChanged(
                            dataRequestEntity, dataRequestPatch.requestStatus,
                            earlierQaApprovedVersionExists, correlationId,
                        )
                    } else {
                        val immediateNotificationWasSent =
                            sendImmediateNotificationOnRequestStatusChangeIfApplicable(
                                dataRequestEntity, dataRequestPatch, earlierQaApprovedVersionExists, correlationId,
                            )
                        resetImmediateNotificationFlagIfApplicable(
                            dataRequestEntity, dataRequestPatch, immediateNotificationWasSent,
                        )
                        notificationService.createUserSpecificNotificationEvent(
                            dataRequestEntity, dataRequestPatch.requestStatus,
                            immediateNotificationWasSent, earlierQaApprovedVersionExists,
                        )
                    }
                }
            }

            val modificationTime = Instant.now().toEpochMilli()
            val anyChanges =
                listOf(
                    updateEmailOnUpdateIfRequired(dataRequestPatch, dataRequestEntity),
                    updateRequestStatusHistoryIfRequired(dataRequestPatch, dataRequestEntity, modificationTime, answeringDataId),
                    updateMessageHistoryIfRequired(dataRequestPatch, dataRequestEntity, modificationTime),
                    checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(dataRequestPatch, dataRequestEntity),
                ).any { it }
            if (anyChanges) dataRequestEntity.lastModifiedDate = modificationTime

            return dataRequestEntity.toStoredDataRequest()
        }

        /**
         * If applicable, send an immediate notification email corresponding to the status change to the user.
         * @returns whether an immediate notification was sent
         */
        private fun sendImmediateNotificationOnRequestStatusChangeIfApplicable(
            dataRequestEntity: DataRequestEntity,
            dataRequestPatch: DataRequestPatch,
            earlierQaApprovedVersionExists: Boolean,
            correlationId: String,
        ): Boolean {
            if (dataRequestEntity.emailOnUpdate) {
                requestEmailManager.sendEmailsWhenRequestStatusChanged(
                    dataRequestEntity, dataRequestPatch.requestStatus, earlierQaApprovedVersionExists, correlationId,
                )
            }
            return dataRequestEntity.emailOnUpdate
        }

        /**
         * Reset the immediate notification flag of the data request entity from true to false if the request status is
         * about to change from Open to Answered.
         */
        private fun resetImmediateNotificationFlagIfApplicable(
            dataRequestEntity: DataRequestEntity,
            dataRequestPatch: DataRequestPatch,
            immediateNotificationWasSent: Boolean,
        ) {
            if (
                immediateNotificationWasSent &&
                dataRequestEntity.requestStatus == RequestStatus.Open &&
                dataRequestPatch.requestStatus == RequestStatus.Answered
            ) {
                dataRequestEntity.emailOnUpdate = false
            }
        }

        /**
         * Change the request status of a given data request to 'Answered'
         * @param dataRequestEntity the entity specifying the data request
         * @param correlationId the correlation ID of the QA event
         */
        private fun patchRequestStatusFromOpenOrNonSourceableToAnsweredByDataRequestEntity(
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
         * Change the request status of all data requests of all subsidiaries of a given company, reporting period and framework
         * @param parentCompanyId the ID of the company whose subsidiaries shall be processed
         * @param reportingPeriod the reporting period for which data requests shall be patched
         * @param dataType the framework for which data requests shall be patched
         * @param answeringDataId the ID of the dataset which triggered this method
         * @param correlationId the correlation ID of the QA event
         */
        private fun patchRequestStatusOfSubsidiariesFromOpenOrNonSourceableToAnswered(
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
                patchRequestStatusFromOpenOrNonSourceableToAnsweredByDataRequestEntity(
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

            patchRequestStatusFromOpenOrNonSourceableToAnsweredForParentAndSubsidiaries(
                dataRequestEntities = openOrNonSourceableDataRequestEntities,
                answeringDataId = dataId,
                correlationId = correlationId,
            )

            processAnsweredOrClosedOrResolvedRequests(
                dataRequestEntities = answeredOrClosedOrResolvedDataRequestEntities,
                correlationId = correlationId,
            )
        }

        /**
         * Method to patch open or non-sourceable data request to answered after a dataset is uploaded
         * @param answeringDataId the id of the uploaded dataset
         * @param correlationId correlationId
         */
        @Transactional
        fun patchRequestStatusFromOpenOrNonSourceableToAnsweredForParentAndSubsidiaries(
            dataRequestEntities: List<DataRequestEntity>,
            answeringDataId: String,
            correlationId: String,
        ) {
            if (dataRequestEntities.isEmpty()) return
            dataRequestEntities.forEach {
                patchRequestStatusFromOpenOrNonSourceableToAnsweredByDataRequestEntity(
                    it, answeringDataId, correlationId,
                )
            }
            // All dataRequestEntities share their companyId, so we only need to take the first.
            val firstDataRequestEntityForParent = dataRequestEntities.first()
            val dataTypeAsEnum = DataTypeEnum.decode(firstDataRequestEntityForParent.dataType)
            if (dataTypeAsEnum == null) {
                throw IllegalArgumentException("Unable to parse data type.")
            }
            patchRequestStatusOfSubsidiariesFromOpenOrNonSourceableToAnswered(
                firstDataRequestEntityForParent.datalandCompanyId,
                firstDataRequestEntityForParent.reportingPeriod,
                dataTypeAsEnum,
                answeringDataId,
                correlationId,
            )
        }

        /**
         * Method to notify users with answered, closed or resolved data requests that a new dataset for the same company,
         * reporting period and framework has been QA-approved.
         *
         * @param dataRequestEntities Correspond to the requests to process.
         * @param correlationId The correlation id of the QA approval event.
         */
        @Transactional
        fun processAnsweredOrClosedOrResolvedRequests(
            dataRequestEntities: List<DataRequestEntity>,
            correlationId: String,
        ) {
            dataRequestEntities.forEach {
                val accessStatusIsOkay = it.accessStatus != AccessStatus.Declined && it.accessStatus != AccessStatus.Revoked
                if (it.emailOnUpdate ||
                    (it.dataType == DataTypeEnum.vsme.name && accessStatusIsOkay)
                ) {
                    requestEmailManager.sendDataUpdatedEmail(
                        it,
                        correlationId,
                    )
                }
                if (it.dataType != DataTypeEnum.vsme.name) {
                    notificationService.createUserSpecificNotificationEvent(
                        dataRequestEntity = it,
                        immediateNotificationWasSent = it.emailOnUpdate,
                        earlierQaApprovedVersionExists = true,
                    )
                }
            }
        }

        /**
         * Method to patch all data request corresponding to a dataset
         * @param nonSourceableInfo the info on the non-sourceable dataset
         * @param correlationId correlationId
         */
        fun patchAllRequestsForThisDatasetToStatusNonSourceable(
            nonSourceableInfo: NonSourceableInfo,
            correlationId: String,
        ) {
            if (nonSourceableInfo.isNonSourceable) {
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
            } else {
                throw IllegalArgumentException(
                    "Expected information about a non-sourceable dataset but received information " +
                        "about a sourceable dataset. No requests are patched if a dataset is reported as " +
                        "sourceable until the dataset is uplaoded.",
                )
            }
        }
    }
