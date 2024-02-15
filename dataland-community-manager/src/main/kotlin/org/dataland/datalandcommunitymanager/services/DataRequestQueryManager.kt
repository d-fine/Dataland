package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.repositories.utils.GetDataRequestsSearchFilter
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundException
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.getDataTypeEnumForFrameworkName
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Implementation of a request manager service for all operations concerning the processing of bulk data requests
 */
@Service
class DataRequestQueryManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
) {
    /** This method retrieves all the data requests for the current user from the database and logs a message.
     * @returns all data requests for the current user
     */
    fun getDataRequestsForUser(): List<StoredDataRequest> {
        val currentUserId = DatalandAuthentication.fromContext().userId
        val retrievedStoredDataRequestEntitiesForUser = dataRequestRepository.fetchMessages(dataRequestRepository.findByUserId(currentUserId))
        val retrievedStoredDataRequestsForUser = retrievedStoredDataRequestEntitiesForUser.map { dataRequestEntity ->
            dataRequestEntity.toStoredDataRequest()
        }
        dataRequestLogger.logMessageForRetrievingDataRequestsForUser()
        return retrievedStoredDataRequestsForUser
    }

    /** This method triggers a query to get aggregated data requests.
     * @param identifierValue can be used to filter via substring matching
     * @param dataTypes can be used to filter on frameworks
     * @returns aggregated data requests
     */
    fun getAggregatedDataRequests(
        identifierValue: String?,
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriod: String?,
    ): List<AggregatedDataRequest> {
        val dataTypesFilterForQuery = if (dataTypes != null && dataTypes.isEmpty()) {
            null
        } else {
            dataTypes?.map { it.value }
        }
        val aggregatedDataRequestEntities =
            dataRequestRepository.getAggregatedDataRequests(identifierValue, dataTypesFilterForQuery, reportingPeriod)
        val aggregatedDataRequests = aggregatedDataRequestEntities.map { aggregatedDataRequestEntity ->
            AggregatedDataRequest(
                getDataTypeEnumForFrameworkName(aggregatedDataRequestEntity.dataType),
                aggregatedDataRequestEntity.reportingPeriod,
                aggregatedDataRequestEntity.dataRequestCompanyIdentifierType,
                aggregatedDataRequestEntity.dataRequestCompanyIdentifierValue,
                aggregatedDataRequestEntity.count,
            )
        }
        return aggregatedDataRequests
    }

    /**
     * Method to retrieve a data request by its ID
     * @param dataRequestId the ID of the data request to retrieve
     * @return the data request corresponding to the provided ID
     */
    @Transactional
    fun getDataRequestById(dataRequestId: String): StoredDataRequest {
        if (!dataRequestRepository.existsById(dataRequestId)) throw DataRequestNotFoundException(dataRequestId)
        val dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        return dataRequestEntity.toStoredDataRequest()
    }

    /**
     * Method to get all data requests based on filters.
     * @param dataType the framework to apply to the data request
     * @param requestStatus the status to apply to the data request
     * @param userId the user to apply to the data request
     * @param reportingPeriod the reporting period to apply to the data request
     * @param dataRequestCompanyIdentifierValue the company identifier value to apply to the data request
     * @return all filtered data requests
     */
    fun getDataRequests(
        dataType: DataTypeEnum?,
        userId: String?,
        requestStatus: RequestStatus?,
        reportingPeriod: String?,
        dataRequestCompanyIdentifierValue: String?,
    ): List<StoredDataRequest>? {
        val filter = GetDataRequestsSearchFilter(
            dataTypeNameFilter = dataType?.name ?: "",
            userIdFilter = userId ?: "",
            requestStatus = requestStatus,
            reportingPeriodFilter = reportingPeriod ?: "",
            dataRequestCompanyIdentifierValueFilter = dataRequestCompanyIdentifierValue ?: "",
        )
        val result = dataRequestRepository.searchDataRequestEntity(filter)

        return dataRequestRepository.fetchMessages(result).map { it.toStoredDataRequest() }
    }
}
