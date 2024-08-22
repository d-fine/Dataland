package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.GetAggregatedRequestsSearchFilter
import org.dataland.datalandcommunitymanager.utils.GetDataRequestsSearchFilter
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

/**
 * Implementation of a request manager service for all request queries
 */
@Service
class DataRequestQueryManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Autowired private val processingUtils: DataRequestProcessingUtils,
) {

    /** This method retrieves all the data requests for the current user from the database and logs a message.
     * @returns all data requests for the current user
     */
    fun getDataRequestsForRequestingUser(): List<ExtendedStoredDataRequest> {
        val currentUserId = DatalandAuthentication.fromContext().userId
        val retrievedStoredDataRequestEntitiesForUser =
            dataRequestRepository.fetchStatusHistory(dataRequestRepository.findByUserId(currentUserId))
        val extendedStoredDataRequests = retrievedStoredDataRequestEntitiesForUser.map { dataRequestEntity ->
            getExtendedStoredDataRequestByRequestEntity(dataRequestEntity)
        }
        dataRequestLogger.logMessageForRetrievingDataRequestsForUser()
        return extendedStoredDataRequests
    }

    /** This method retrieves an extended stored data request based on a data request entity
     * @param dataRequestEntity dataland data request entity
     * @returns extended stored data request
     */
    fun getExtendedStoredDataRequestByRequestEntity(dataRequestEntity: DataRequestEntity): ExtendedStoredDataRequest {
        val companyInformation = companyDataControllerApi.getCompanyInfo(dataRequestEntity.datalandCompanyId)
        return ExtendedStoredDataRequest(dataRequestEntity, companyInformation.companyName)
    }

    /** This method triggers a query to get aggregated data requests.
     * @param identifierValue can be used to filter via substring matching
     * @param dataTypes can be used to filter on frameworks
     * @param reportingPeriod can be used to filter on reporting periods
     * @param status can be used to filter on request status
     * @returns aggregated data requests
     */
    fun getAggregatedDataRequests(
        identifierValue: String?,
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriod: String?,
        status: RequestStatus?,
    ): List<AggregatedDataRequest> {
        val dataTypesFilterForQuery = if (dataTypes != null && dataTypes.isEmpty()) {
            null
        } else {
            dataTypes?.map { it.value }?.toSet()
        }
        val aggregatedDataRequestEntities =
            dataRequestRepository.getAggregatedDataRequests(
                GetAggregatedRequestsSearchFilter(
                    dataTypeFilter = dataTypesFilterForQuery,
                    reportingPeriodFilter = reportingPeriod,
                    requestStatus = status?.name ?: "",
                    datalandCompanyIdFilter = identifierValue,
                ),
            )
        val aggregatedDataRequests = aggregatedDataRequestEntities.map { aggregatedDataRequestEntity ->
            AggregatedDataRequest(
                processingUtils.getDataTypeEnumForFrameworkName(aggregatedDataRequestEntity.dataType),
                aggregatedDataRequestEntity.reportingPeriod,
                aggregatedDataRequestEntity.datalandCompanyId,
                aggregatedDataRequestEntity.requestStatus,
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
        val dataRequestEntity = dataRequestRepository.findById(dataRequestId).getOrElse {
            throw DataRequestNotFoundApiException(dataRequestId)
        }
        return dataRequestEntity.toStoredDataRequest()
    }

    /**
     * Method to get all data requests based on filters.
     * @param dataType the framework to apply to the data request
     * @param requestStatus the status to apply to the data request
     * @param userId the user to apply to the data request
     * @param reportingPeriod the reporting period to apply to the data request
     * @param datalandCompanyId the Dataland company ID to apply to the data request
     * @return all filtered data requests
     */
    @Transactional
    fun getDataRequests(
        dataType: DataTypeEnum?,
        userId: String?,
        requestStatus: RequestStatus?,
        accessStatus: AccessStatus?,
        reportingPeriod: String?,
        datalandCompanyId: String?,
    ): List<ExtendedStoredDataRequest>? {
        val filter = GetDataRequestsSearchFilter(
            dataTypeFilter = dataType?.value ?: "",
            userIdFilter = userId ?: "",
            requestStatus = requestStatus?.name ?: "",
            accessStatus = accessStatus?.name ?: "",
            reportingPeriodFilter = reportingPeriod ?: "",
            datalandCompanyIdFilter = datalandCompanyId ?: "",
        )
        return dataRequestRepository.searchDataRequestEntityAndStatusHistory(filter).map { dataRequestEntity ->
            getExtendedStoredDataRequestByRequestEntity(dataRequestEntity)
        }
    }
}
