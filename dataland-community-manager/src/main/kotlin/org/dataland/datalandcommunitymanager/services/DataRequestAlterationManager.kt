package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestEmailSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
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
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Autowired private val dataRequestEmailSender: DataRequestEmailSender,
) {
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
            val companyName = companyDataControllerApi.getCompanyInfo(dataRequestEntity.datalandCompanyId).companyName
            dataRequestEmailSender.sendDataRequestedAnsweredEmail(dataRequestEntity, companyName)
        }
        return dataRequestEntity.toStoredDataRequest()
    }
}
