package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequestWithAggregatedPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedRequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestMasker
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
        private val keycloakUserControllerApiService: KeycloakUserService,
        private val dataRequestMasker: DataRequestMasker,
        private val requestPriorityAggregator: RequestPriorityAggregator,
    ) {
        /** This method retrieves all the data requests for the current user from the database and logs a message.
         * @returns all data requests for the current user
         */
        fun getDataRequestsForRequestingUser(): List<ExtendedStoredDataRequest> {
            val currentUserId = DatalandAuthentication.fromContext().userId
            val retrievedStoredDataRequestEntitiesForUser =
                dataRequestRepository.fetchStatusHistory(dataRequestRepository.findByUserId(currentUserId))

            val extendedStoredDataRequests =
                retrievedStoredDataRequestEntitiesForUser.map { dataRequestEntity ->
                    convertRequestEntityToExtendedStoredDataRequest(dataRequestEntity)
                }

            val extendedStoredDataRequestsFilteredAdminComment =
                dataRequestMasker
                    .hideAdminCommentForNonAdmins(extendedStoredDataRequests)

            dataRequestLogger.logMessageForRetrievingDataRequestsForUser()
            return extendedStoredDataRequestsFilteredAdminComment
        }

        /** This method retrieves an extended stored data request based on a data request entity
         * @param dataRequestEntity data request entity
         * @returns extended stored data request
         */
        private fun convertRequestEntityToExtendedStoredDataRequest(dataRequestEntity: DataRequestEntity): ExtendedStoredDataRequest {
            val companyInformation = companyDataControllerApi.getCompanyInfo(dataRequestEntity.datalandCompanyId)
            return ExtendedStoredDataRequest(dataRequestEntity, companyInformation.companyName, null)
        }

        /** This method triggers a query to get aggregated open data requests.
         * @param identifierValue can be used to filter via substring matching
         * @param dataTypes can be used to filter on frameworks
         * @param reportingPeriod can be used to filter on reporting periods
         * @returns aggregated open data requests
         */
        fun getAggregatedOpenDataRequests(
            identifierValue: String?,
            dataTypes: Set<DataTypeEnum>?,
            reportingPeriod: String?,
        ): List<AggregatedDataRequest> {
            val dataTypesFilterForQuery =
                if (dataTypes != null && dataTypes.isEmpty()) {
                    null
                } else {
                    dataTypes?.map { it.value }?.toSet()
                }
            val aggregatedDataRequestEntities =
                dataRequestRepository.getAggregatedDataRequests(
                    GetAggregatedRequestsSearchFilter(
                        dataTypeFilter = dataTypesFilterForQuery ?: setOf(),
                        requestStatus = RequestStatus.Open.toString(),
                        reportingPeriodFilter = reportingPeriod,
                        priority = null,
                        datalandCompanyIdFilter = identifierValue,
                    ),
                )
            val aggregatedDataRequests =
                aggregatedDataRequestEntities.map { aggregatedDataRequestEntity ->
                    AggregatedDataRequest(
                        processingUtils.getDataTypeEnumForFrameworkName(aggregatedDataRequestEntity.dataType),
                        aggregatedDataRequestEntity.reportingPeriod,
                        aggregatedDataRequestEntity.datalandCompanyId,
                        RequestPriority.valueOf(aggregatedDataRequestEntity.priority),
                        aggregatedDataRequestEntity.requestStatus,
                        aggregatedDataRequestEntity.count,
                    )
                }

            return aggregatedDataRequests
        }

        /** This method triggers a query to get all aggregated open data requests
         * including the aggregated request priority
         * @param dataTypes can be used to filter on frameworks
         * @param reportingPeriod can be used to filter on reporting periods
         * @param aggregatedPriority can be used to filter on aggregated priorities
         * @returns all aggregated open data requests with the aggregated request priority
         */
        fun getAggregatedOpenDataRequestsWithAggregatedRequestPriority(
            dataTypes: Set<DataTypeEnum>?,
            reportingPeriod: String?,
            aggregatedPriority: AggregatedRequestPriority?,
        ): List<AggregatedDataRequestWithAggregatedPriority> {
            val aggregatedDataRequestsAllCompanies = getAggregatedOpenDataRequests(identifierValue = null, dataTypes, reportingPeriod)
            val aggregatedRequestsWithAggregatedPriority =
                requestPriorityAggregator.aggregateRequestPriority(aggregatedDataRequestsAllCompanies)

            val filteredAggregatedRequestsWithAggregatedPriority =
                requestPriorityAggregator.filterBasedOnAggregatedPriority(
                    aggregatedRequestsWithAggregatedPriority,
                    aggregatedPriority,
                )

            return filteredAggregatedRequestsWithAggregatedPriority
        }

        /**
         * Method to retrieve a data request by its ID
         * @param dataRequestId the ID of the data request to retrieve
         * @return the data request corresponding to the provided ID
         */
        @Transactional
        fun getDataRequestById(dataRequestId: String): StoredDataRequest {
            val dataRequestEntity =
                dataRequestRepository.findById(dataRequestId).getOrElse {
                    throw DataRequestNotFoundApiException(dataRequestId)
                }

            val emailAddress = keycloakUserControllerApiService.getUser(dataRequestEntity.userId).email ?: ""
            val storedDataRequest = dataRequestEntity.toStoredDataRequest(emailAddress)

            val storedDataRequestsFilteredAdminComment = dataRequestMasker.hideAdminCommentForNonAdmins(storedDataRequest)

            return storedDataRequestsFilteredAdminComment
        }

        /**
         * Method to get all data requests based on filters.
         * @param ownedCompanyIdsByUser the company ids for which the user is a company owner
         * @param filter the search filter containing relevant search parameters
         * @param chunkIndex the index of the chunked results which should be returned
         * @param chunkSize the size of entries per chunk which should be returned
         * @return all filtered data requests
         */
        @Transactional
        fun getDataRequests(
            ownedCompanyIdsByUser: List<String>,
            filter: DataRequestsFilter,
            chunkIndex: Int?,
            chunkSize: Int?,
        ): List<ExtendedStoredDataRequest>? {
            val offset = (chunkIndex ?: 0) * (chunkSize ?: 0)

            val extendedStoredDataRequests =
                dataRequestRepository
                    .searchDataRequestEntity(
                        searchFilter = filter, resultOffset = offset, resultLimit = chunkSize,
                    ).map { dataRequestEntity -> convertRequestEntityToExtendedStoredDataRequest(dataRequestEntity) }

            val extendedStoredDataRequestsWithMails =
                dataRequestMasker.addEmailAddressIfAllowedToSee(
                    extendedStoredDataRequests, ownedCompanyIdsByUser, filter,
                )
            val extendedStoredDataRequestsFilteredAdminComment =
                dataRequestMasker.hideAdminCommentForNonAdmins(extendedStoredDataRequestsWithMails)

            return extendedStoredDataRequestsFilteredAdminComment
        }

        /**
         * Returns the number of requests for a specific filter.
         */
        @Transactional
        fun getNumberOfDataRequests(filter: DataRequestsFilter): Int {
            filter.setupEmailAddressFilter(keycloakUserControllerApiService)
            return dataRequestRepository.getNumberOfRequests(filter)
        }
    }
