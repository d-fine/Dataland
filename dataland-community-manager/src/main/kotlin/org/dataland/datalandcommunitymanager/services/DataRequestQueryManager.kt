package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.CompanyRoleAssignmentEntity
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsQueryFilter
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
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
    private val objectMapper: ObjectMapper,
    @Qualifier("AuthenticatedOkHttpClient") val authenticatedOkHttpClient: OkHttpClient,
    @Value("\${dataland.keycloak.base-url}") private val keycloakBaseUrl: String,
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
                identifierValue,
                dataTypesFilterForQuery,
                reportingPeriod,
                status,
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class User( // TODO backend utils?
        @JsonProperty("email")
        val email: String?,
    )

    /**
     * Gets the email address of a user in keycloak given the user id
     * @param userId the userId of the user in question
     * @returns the email address
     */
    fun getEmailAddress(userId: String): String {
        // TODO duplicate code to KeycloakUserControllerApiService => centralize?
        val request = Request.Builder()
            .url("$keycloakBaseUrl/admin/realms/datalandsecurity/users/$userId")
            .build()
        val response = authenticatedOkHttpClient.newCall(request).execute()
        val parsedResponseBody = objectMapper.readValue(
            response.body!!.string(),
            User::class.java,
        )
        return parsedResponseBody.email ?: ""
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
        filter: DataRequestsQueryFilter,
        companyRoleAssignmentsOfCurrentUser: List<CompanyRoleAssignmentEntity>,
    ): List<StoredDataRequest>? {
        return createStoredDataRequestObjects(filter, companyRoleAssignmentsOfCurrentUser)
    }

    /**
     * Fetches data requests from the database and returns them as api model objects.
     * The email addresses of the users associated with the data requests are only included for
     * those companies for which the current user is a company owner.
     * @param filter to retrieve only specific data requests
     * @param companyRoleAssignmentsOfCurrentUser contains the company ownerships of the current user
     * @return all filtered data requests as api model objects
     */
    private fun createStoredDataRequestObjects(
        filter: DataRequestsQueryFilter,
        companyRoleAssignmentsOfCurrentUser: List<CompanyRoleAssignmentEntity>,
    ): List<StoredDataRequest> {
        val ownedCompanyIds = companyRoleAssignmentsOfCurrentUser.filter {
            it.companyRole == CompanyRole.CompanyOwner
        }.map { it.companyId }

        val queryResult = dataRequestRepository.searchDataRequestEntity(filter)
        val queryResultWithHistory = dataRequestRepository.fetchStatusHistory(queryResult)

        val storedDataRequests = queryResultWithHistory.map {
            val allowedToSeeEmailAddress =
                ownedCompanyIds.contains(it.datalandCompanyId) && it.accessStatus != AccessStatus.Public
            var emailAddress: String? = null
            if (allowedToSeeEmailAddress) {
                emailAddress = getEmailAddress(it.userId)
            }
            it.toStoredDataRequest(emailAddress)
        }
        return storedDataRequests
    }
}
