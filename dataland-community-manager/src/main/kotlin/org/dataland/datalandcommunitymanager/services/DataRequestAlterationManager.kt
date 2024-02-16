package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Manages all alterations of data requests
 */
@Service
class DataRequestAlterationManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
) {
    /**
     * Method to patch the status of a data request.
     * @param dataRequestId the id of the data request to patch
     * @param requestStatus the status to apply to the data request
     * @return the updated data request object
     */
    @Transactional
    fun patchDataRequestStatus(dataRequestId: String, requestStatus: RequestStatus): StoredDataRequest {
        if (!dataRequestRepository.existsById(dataRequestId)) throw DataRequestNotFoundApiException(dataRequestId)
        var dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        dataRequestLogger.logMessageForPatchingRequestStatus(dataRequestEntity.dataRequestId, requestStatus)
        dataRequestEntity.requestStatus = requestStatus
        dataRequestRepository.save(dataRequestEntity)
        dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        return dataRequestEntity.toStoredDataRequest()
    }
}
