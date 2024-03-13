package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestedAnsweredEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.GetDataRequestsSearchFilter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import kotlin.jvm.optionals.getOrElse

/**
 * Manages all alterations of data requests
 */
@Service
class DataRequestAlterationManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val dataRequestedAnsweredEmailMessageSender: DataRequestedAnsweredEmailMessageSender,
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
) {
    private val logger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

    /**
     * Method to patch the status of a data request.
     * @param dataRequestId the id of the data request to patch
     * @param requestStatus the status to apply to the data request
     * @return the updated data request object
     */
    @Transactional
    fun patchDataRequestStatus(
        dataRequestId: String,
        requestStatus: RequestStatus,
    ): StoredDataRequest {
        val dataRequestEntity = dataRequestRepository.findById(dataRequestId).getOrElse {
            throw DataRequestNotFoundApiException(dataRequestId)
        }
        dataRequestLogger.logMessageForPatchingRequestStatus(dataRequestEntity.dataRequestId, requestStatus)
        dataRequestEntity.requestStatus = requestStatus
        dataRequestEntity.lastModifiedDate = Instant.now().toEpochMilli()
        dataRequestRepository.save(dataRequestEntity)
        if (requestStatus == RequestStatus.Answered) {
            val correlationId = UUID.randomUUID().toString()
            dataRequestedAnsweredEmailMessageSender.sendDataRequestedAnsweredEmail(dataRequestEntity, correlationId)
        }
        return dataRequestEntity.toStoredDataRequest()
    }

    fun patchRequestStatusFromOpenToAnsweredByDataId(dataId: String, correlationId: String) {
        val metaData = metaDataControllerApi.getDataMetaInfo(dataId)
        val dataRequestEntities = dataRequestRepository.searchDataRequestEntity(
            GetDataRequestsSearchFilter(
                metaData.dataType.value, "", RequestStatus.Open, metaData.reportingPeriod, metaData.companyId,
            ),
        )
        dataRequestRepository.updateDataRequestEntitiesFromOpenToAnswered(
            metaData.companyId, metaData.reportingPeriod, metaData.dataType.value,
        )
        dataRequestEntities.forEach {
            dataRequestedAnsweredEmailMessageSender.sendDataRequestedAnsweredEmail(it, correlationId)
        }
        logger.info(
            "Changed Request Status for company Id ${metaData.companyId}, " +
                "reporting period ${metaData.reportingPeriod} and framework ${metaData.dataType.name}",
        )
    }
}
