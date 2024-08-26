package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.GetDataRequestsSearchFilter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.jvm.optionals.getOrElse

/**
 * Manages all alterations of data requests
 */
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
     * Method to patch the status of a data request.
     * @param dataRequestId the id of the data request to patch
     * @param requestStatus the status to apply to the data request
     * @return the updated data request object
     */
    @Transactional
    fun patchDataRequest(
        dataRequestId: String,
        requestStatus: RequestStatus? = null,
        accessStatus: AccessStatus? = null,
        contacts: Set<String>? = null,
        message: String? = null,
        correlationId: String? = null,
    ): StoredDataRequest {
        val dataRequestEntity = dataRequestRepository.findById(dataRequestId).getOrElse {
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
        if (anyChanges) dataRequestEntity.lastModifiedDate = modificationTime
        requestEmailManager.sendEmailsWhenStatusChanged(dataRequestEntity, requestStatus, accessStatus, correlationId)

        return dataRequestEntity.toStoredDataRequest()
    }

    /**
     * Method to patch open data request to answered after a dataset is uploaded
     * @param dataId the id of the uploaded dataset
     * @param correlationId dataland correlationId
     */
    @Transactional
    fun patchRequestStatusFromOpenToAnsweredByDataId(dataId: String, correlationId: String) {
        val metaData = metaDataControllerApi.getDataMetaInfo(dataId)
        val dataRequestEntities = dataRequestRepository.searchDataRequestEntity(
            GetDataRequestsSearchFilter(
                dataTypeFilter = metaData.dataType.value, userIdFilter = "", requestStatus = RequestStatus.Open.name,
                accessStatus = null, reportingPeriodFilter = metaData.reportingPeriod,
                datalandCompanyIdFilter = metaData.companyId,
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
