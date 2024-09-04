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
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.dataland.datalandcommunitymanager.utils.GetAggregatedRequestsSearchFilter
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

/**
 * Implementation of a request manager service for all request queries
 */
@Service
class DataRequestQueryManager
@Suppress("LongParameterList")
@Autowired
constructor(
    private val dataRequestRepository: DataRequestRepository,
    private val dataRequestLogger: DataRequestLogger,
    private val companyDataControllerApi: CompanyDataControllerApi,
    private val processingUtils: DataRequestProcessingUtils,
    private val keycloakUserControllerApiService: KeycloakUserControllerApiService,
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
        return ExtendedStoredDataRequest(dataRequestEntity, companyInformation.companyName, null)
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
                    dataTypeFilter = dataTypesFilterForQuery ?: setOf(),
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
     * @param isUserAdmin TODO
     * @param ownedCompanyIdsByUser TODO
     * @param filter the search filter containing relevant search parameters
     * @param chunkIndex the index of the chunked results which should be returned
     * @param chunkSize the size of entries per chunk which should be returned
     * @return all filtered data requests
     */
    @Transactional
    fun getDataRequests(
        isUserAdmin: Boolean,
        ownedCompanyIdsByUser: List<String>,
        filter: DataRequestsFilter,
        chunkIndex: Int?,
        chunkSize: Int?,
    ): List<ExtendedStoredDataRequest>? {
        val offset = (chunkIndex ?: 0) * (chunkSize ?: 0)
        val extendedStoredDataRequest = dataRequestRepository.searchDataRequestEntity(
            searchFilter = filter, resultOffset = offset, resultLimit = chunkSize,
        ).map { dataRequestEntity ->
            getExtendedStoredDataRequestByRequestEntity(dataRequestEntity)
        }

        val userInfoList = filter.setupEmailFilter(keycloakUserControllerApiService)
        val userEmailMap = userInfoList.associate { it.userId to it.email }.toMutableMap()

        val storedDataRequests = extendedStoredDataRequest.map { storedDataRequest ->
            val allowedToSeeEmailAddress = isUserAdmin ||
                (
                    ownedCompanyIdsByUser.contains(storedDataRequest.datalandCompanyId) &&
                        storedDataRequest.accessStatus != AccessStatus.Public
                    )

            storedDataRequest.userEmailAddress = storedDataRequest.userId
                .takeIf { allowedToSeeEmailAddress }
                ?.let { userEmailMap.getOrPut(it) { keycloakUserControllerApiService.getUser(it).email ?: "" } }

            storedDataRequest
        }
        return storedDataRequests
    }

    /**
     * TODO
     */
    @Transactional
    fun getNumberOfDataRequests(
        filter: DataRequestsFilter,
    ): Int {
        filter.setupEmailFilter(keycloakUserControllerApiService)
        return dataRequestRepository.getNumberOfRequests(filter)
    }
}
