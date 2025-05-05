package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.services.CompanyRolesManager
import org.dataland.datalandcommunitymanager.services.RequestEmailManager
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi.DataTypesGetInfoOnDatasets
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * A helper class for DataRequestUpdateManager.
 */
@Service
class DataRequestUpdateUtils
    @Autowired
    constructor(
        private val dataRequestProcessingUtils: DataRequestProcessingUtils,
        private val dataRequestLogger: DataRequestLogger,
        private val companyInfoService: CompanyInfoService,
        private val companyRolesManager: CompanyRolesManager,
        private val requestEmailManager: RequestEmailManager,
        private val qaControllerApi: QaControllerApi,
    ) {
        /**
         * Patches the email-on-update-field if requestStatus changes and returns if it was updated.
         * @param dataRequestPatch the DataRequestPatch holding the data to be changed
         * @param dataRequestEntity the old DataRequestEntity that is to be changed
         * @return true if the notifyMeImmediately was updated, false otherwise
         */

        fun updateNotifyMeImmediatelyIfRequired(
            dataRequestPatch: DataRequestPatch,
            dataRequestEntity: DataRequestEntity,
        ): Boolean =
            if (dataRequestPatch.notifyMeImmediately != null
            ) {
                dataRequestEntity.notifyMeImmediately = dataRequestPatch.notifyMeImmediately!!
                true
            } else if (dataRequestPatch.requestStatus != null &&
                dataRequestPatch.requestStatus != dataRequestEntity.requestStatus &&
                dataRequestEntity.notifyMeImmediately
            ) {
                dataRequestEntity.notifyMeImmediately = false
                true
            } else {
                false
            }

        /**
         * Updates the request status history if the request status changed and returns
         * whether it was updated.
         * @param dataRequestPatch the DataRequestPatch holding the data to be changed
         * @param dataRequestEntity the old DataRequestEntity that is to be changed
         * @param modificationTime the modification time in unix epoch milliseconds
         * @return true if the request status history was updated, false otherwise
         */
        fun updateRequestStatusHistoryIfRequired(
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
                dataRequestProcessingUtils.addNewRequestStatusToHistory(
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
         * Updates the message history if valid contacts are passed in the patch object and returns
         * whether it was updated.
         * @param dataRequestPatch the DataRequestPatch holding the data to be changed
         * @param dataRequestEntity the old DataRequestEntity that is to be changed
         * @param modificationTime the modification time in unix epoch milliseconds
         * @return true if message history was updated, false otherwise
         */
        fun updateMessageHistoryIfRequired(
            dataRequestPatch: DataRequestPatch,
            dataRequestEntity: DataRequestEntity,
            modificationTime: Long,
        ): Boolean {
            val filteredContacts = dataRequestPatch.contacts.takeIf { !it.isNullOrEmpty() }
            val filteredMessage = dataRequestPatch.message.takeIf { !it.isNullOrEmpty() }
            filteredContacts?.forEach {
                MessageEntity.validateContact(it, companyRolesManager, dataRequestEntity.datalandCompanyId)
            }
            return filteredContacts?.let {
                dataRequestProcessingUtils.addMessageToMessageHistory(dataRequestEntity, it, filteredMessage, modificationTime)
                requestEmailManager.sendSingleDataRequestEmail(dataRequestEntity, it, filteredMessage)
                dataRequestLogger.logMessageForPatchingRequestMessage(dataRequestEntity.dataRequestId)
                true
            } ?: false
        }

        /**
         * Creates log messages if required and returns whether the request priority changed
         * @param dataRequestPatch the DataRequestPatch holding the data to be changed
         * @param dataRequestEntity the old DataRequestEntity that is to be changed
         * @return whether the request priority changed
         */
        fun checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(
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
        fun existsEarlierQaApprovalOfDatasetForDataDimension(dataRequestEntity: DataRequestEntity): Boolean {
            val dataTypeAsDataTypesGetInfoOnDatasets =
                DataTypesGetInfoOnDatasets.entries.first {
                    it.toString() == dataRequestEntity.dataType
                }

            return qaControllerApi
                .getInfoOnDatasets(
                    dataTypes = listOf(dataTypeAsDataTypesGetInfoOnDatasets),
                    reportingPeriods = setOf(dataRequestEntity.reportingPeriod),
                    companyName = companyInfoService.getValidCompanyName(dataRequestEntity.datalandCompanyId),
                    qaStatus = QaStatus.Accepted,
                ).size > 1
        }
    }
