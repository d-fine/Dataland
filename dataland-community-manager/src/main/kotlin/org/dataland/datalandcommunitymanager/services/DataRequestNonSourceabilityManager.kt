package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.NonSourceabilityInformationResponse
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestUpdateUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.dataland.datalandmessagequeueutils.messages.SourceabilityMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Manages patching of data requests to status non-sourceable
 */
@Service
class DataRequestNonSourceabilityManager
    @Autowired
    constructor(
        private val dataRequestRepository: DataRequestRepository,
        private val metaDataControllerApi: MetaDataControllerApi,
        private val requestEmailManager: RequestEmailManager,
        private val dataRequestSummaryNotificationService: DataRequestSummaryNotificationService,
        private val dataRequestUpdateUtils: DataRequestUpdateUtils,
    ) {
        /**
         * Method to patch all non-withdrawn data requests corresponding to a dataset to status non-sourceable.
         * @param sourceabilityInfo the info on the non-sourceable dataset
         */
        @Transactional
        fun patchAllNonWithdrawnRequestsToStatusNonSourceable(
            sourceabilityInfo: SourceabilityMessage,
            correlationId: String,
        ) {
            require(sourceabilityInfo.isNonSourceable) {
                "Expected information about a non-sourceable dataset but received information about a sourceable dataset. No requests " +
                    "are patched if a dataset is reported as sourceable until the dataset is uploaded."
            }

            patchAllNonWithdrawnRequestsToStatusNonSourceable(
                companyId = sourceabilityInfo.basicDataDimensions.companyId,
                dataTypeAsString = sourceabilityInfo.basicDataDimensions.dataType,
                reportingPeriod = sourceabilityInfo.basicDataDimensions.reportingPeriod,
                correlationId = correlationId,
                requestStatusChangeReason = sourceabilityInfo.reason,
            )
        }

        /**
         * Method to patch all non-withdrawn data requests corresponding to a dataset to status non-sourceable.
         * This entrypoint is used for lifecycle events where no reason is present in the MQ payload.
         */
        @Transactional
        fun patchAllNonWithdrawnRequestsToStatusNonSourceable(
            companyId: String,
            dataTypeAsString: String,
            reportingPeriod: String,
            correlationId: String,
            requestStatusChangeReason: String? = null,
        ) {
            val dataType =
                DataTypeEnum.decode(dataTypeAsString)
                    ?: throw IllegalArgumentException(
                        "Unsupported data type '$dataTypeAsString' in non-sourceability message.",
                    )

            val resolvedReason =
                requestStatusChangeReason
                    ?: getLatestActiveNonSourceabilityReason(
                        companyId = companyId,
                        dataType = dataType,
                        reportingPeriod = reportingPeriod,
                    )

            val dataRequestEntities =
                dataRequestRepository.searchDataRequestEntity(
                    DataRequestsFilter(
                        dataType = setOf(dataType),
                        datalandCompanyIds = setOf(companyId),
                        reportingPeriods = setOf(reportingPeriod),
                        requestStatus = RequestStatus.entries.filter { it != RequestStatus.Withdrawn }.toSet(),
                    ),
                )

            dataRequestEntities.forEach { patchEntityToNonSourceable(it, resolvedReason, correlationId) }
        }

        private fun getLatestActiveNonSourceabilityReason(
            companyId: String,
            dataType: DataTypeEnum,
            reportingPeriod: String,
        ): String? {
            val entries =
                metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(
                    companyId = companyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                )
            return entries
                .asSequence()
                .filter(NonSourceabilityInformationResponse::currentlyActive)
                .maxByOrNull(NonSourceabilityInformationResponse::uploadTime)
                ?.reason
        }

        private fun patchEntityToNonSourceable(
            dataRequestEntity: DataRequestEntity,
            requestStatusChangeReason: String?,
            correlationId: String,
        ) {
            val patch =
                DataRequestPatch(
                    requestStatus = RequestStatus.NonSourceable,
                    requestStatusChangeReason = requestStatusChangeReason,
                )
            val earlierQaApproval = dataRequestUpdateUtils.existsEarlierQaApprovalOfDatasetForDataDimension(dataRequestEntity)
            if (dataRequestEntity.dataType == DataTypeEnum.vsme.name) {
                requestEmailManager.sendNotificationsSpecificToAccessRequests(dataRequestEntity, patch, correlationId)
                requestEmailManager.sendEmailsWhenRequestStatusChanged(
                    dataRequestEntity, RequestStatus.NonSourceable, requestStatusChangeReason, earlierQaApproval, correlationId,
                )
            } else {
                if (dataRequestEntity.notifyMeImmediately) {
                    requestEmailManager.sendEmailsWhenRequestStatusChanged(
                        dataRequestEntity, RequestStatus.NonSourceable, requestStatusChangeReason, earlierQaApproval, correlationId,
                    )
                }
                dataRequestSummaryNotificationService.createUserSpecificNotificationEvent(
                    dataRequestEntity, RequestStatus.NonSourceable, dataRequestEntity.notifyMeImmediately, earlierQaApproval,
                )
            }
            updateEntity(dataRequestEntity, patch)
        }

        private fun updateEntity(
            dataRequestEntity: DataRequestEntity,
            patch: DataRequestPatch,
        ) {
            val modificationTime = Instant.now().toEpochMilli()
            val anyChanges =
                listOf(
                    dataRequestUpdateUtils.updateNotifyMeImmediatelyIfRequired(patch, dataRequestEntity),
                    dataRequestUpdateUtils.updateRequestStatusHistoryIfRequired(patch, dataRequestEntity, modificationTime, null),
                    dataRequestUpdateUtils.updateMessageHistoryIfRequired(patch, dataRequestEntity, modificationTime),
                    dataRequestUpdateUtils.checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(patch, dataRequestEntity),
                ).any { it }
            if (anyChanges) dataRequestEntity.lastModifiedDate = modificationTime
            dataRequestRepository.save(dataRequestEntity)
        }
    }
