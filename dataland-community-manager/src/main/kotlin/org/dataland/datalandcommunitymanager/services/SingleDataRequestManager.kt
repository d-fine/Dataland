package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.repositories.utils.GetDataRequestsSearchFilter
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestManagerUtils
import org.dataland.datalandemail.email.isEmailAddress
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of a request manager service for all operations concerning the processing of single data requests
 */
@Service("SingleDataRequestManager")
class SingleDataRequestManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val companyGetter: CompanyGetter,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val singleDataRequestEmailSender: SingleDataRequestEmailSender,
) {
    private val utils = DataRequestManagerUtils(dataRequestRepository, dataRequestLogger, companyGetter, objectMapper)
    val companyIdRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")

    /**
     * Processes a single data request from a user
     * @param singleDataRequest info provided by a user in order to request a single dataset on Dataland
     * @return the stored data request object
     */
    @Transactional
    fun processSingleDataRequest(singleDataRequest: SingleDataRequest): List<StoredDataRequest> {
        if (DatalandAuthentication.fromContext() !is DatalandJwtAuthentication) {
            throw AuthenticationMethodNotSupportedException("You are not using JWT authentication.")
        }
        assertValidMessage(singleDataRequest)
        val storedDataRequests = mutableListOf<StoredDataRequest>()
        val (identifierTypeToStore, identifierValueToStore) = identifyIdentifierTypeAndTryGetDatalandCompanyId(
            singleDataRequest.companyIdentifier,
        )
        storeDataRequestsAndAddThemToListForEachReportingPeriodIfNotAlreadyExisting(
            storedDataRequests, singleDataRequest, identifierValueToStore, identifierTypeToStore,
        )
        singleDataRequestEmailSender.sendSingleDataRequestEmails(
            userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
            singleDataRequest = singleDataRequest,
            companyIdentifierType = identifierTypeToStore,
            companyIdentifierValue = identifierValueToStore,
        )
        return storedDataRequests
    }

    private fun assertValidMessage(singleDataRequest: SingleDataRequest) {
        val contacts = singleDataRequest.contactList
        if (!contacts.isNullOrEmpty() && contacts.any { !it.isEmailAddress() }) {
            throw InvalidInputApiException(
                "You must provide proper email addresses as contacts.",
                "You must provide proper email addresses as contacts.",
            )
        }
        if (contacts.isNullOrEmpty() && !singleDataRequest.message.isNullOrBlank()) {
            throw InvalidInputApiException(
                "Insufficient information to create message object.",
                "Without at least one proper email address being provided no message can be forwarded.",
            )
        }
    }

    private fun identifyIdentifierTypeAndTryGetDatalandCompanyId(
        companyIdentifier: String,
    ): Pair<DataRequestCompanyIdentifierType, String> {
        if (companyIdRegex.matches(companyIdentifier)) {
            checkIfCompanyIsValid(companyIdentifier)
            return Pair(DataRequestCompanyIdentifierType.DatalandCompanyId, companyIdentifier)
        }
        val matchedIdentifierType = utils.determineIdentifierTypeViaRegex(companyIdentifier)
        dataRequestLogger.logMessageForReceivingSingleDataRequest(companyIdentifier)
        if (matchedIdentifierType != null) {
            val datalandCompanyId = utils.getDatalandCompanyIdForIdentifierValue(
                companyIdentifier,
            )
            return Pair(
                datalandCompanyId?.let {
                    DataRequestCompanyIdentifierType.DatalandCompanyId
                } ?: matchedIdentifierType,
                datalandCompanyId ?: companyIdentifier,
            )
        }
        throw InvalidInputApiException(
            "The provided company identifier has an invalid format.",
            "The company identifier you provided does not match the patterns " +
                "of a valid LEI, ISIN, PermId or Dataland CompanyID.",
        )
    }

    private fun checkIfCompanyIsValid(companyId: String) {
        val bearerTokenOfRequestingUser = DatalandAuthentication.fromContext().credentials as String
        try {
            companyGetter.getCompanyById(companyId, bearerTokenOfRequestingUser)
        } catch (e: ClientException) { if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
            throw ResourceNotFoundApiException(
                "Company not found",
                "Dataland-backend does not know the company ID $companyId",
            )
        }
        }
    }

    private fun storeDataRequestsAndAddThemToListForEachReportingPeriodIfNotAlreadyExisting(
        storedDataRequests: MutableList<StoredDataRequest>,
        singleDataRequest: SingleDataRequest,
        identifierValueToStore: String,
        identifierTypeToStore: DataRequestCompanyIdentifierType,
    ) {
        for (reportingPeriod in singleDataRequest.listOfReportingPeriods.distinct()) {
            storedDataRequests.add(
                utils.buildStoredDataRequestFromDataRequestEntity(
                    utils.storeDataRequestEntityIfNotExisting(
                        identifierValueToStore,
                        identifierTypeToStore,
                        singleDataRequest.frameworkName,
                        reportingPeriod,
                        singleDataRequest.contactList,
                        singleDataRequest.message,
                    ),
                ),
            )
        }
    }

    /**
     * Method to retrieve a data request by its ID
     * @param dataRequestId the ID of the data request to retrieve
     * @return the data request corresponding to the provided ID
     */
    @Transactional
    fun getDataRequestById(dataRequestId: String): StoredDataRequest {
        throwResourceNotFoundExceptionIfDataRequestIdUnknown(dataRequestId)
        val dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        return utils.buildStoredDataRequestFromDataRequestEntity(dataRequestEntity)
    }

    private fun throwResourceNotFoundExceptionIfDataRequestIdUnknown(dataRequestId: String) {
        if (!dataRequestRepository.existsById(dataRequestId)) {
            throw ResourceNotFoundApiException(
                "Data request not found",
                "Dataland does not know the Data request ID $dataRequestId",
            )
        }
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

        return result.map { utils.buildStoredDataRequestFromDataRequestEntity(it) }
    }

    /**
     * Method to patch the status of a data request.
     * @param dataRequestId the id of the data request to patch
     * @param requestStatus the status to apply to the data request
     * @return the updated data request object
     */
    @Transactional
    fun patchDataRequest(dataRequestId: String, requestStatus: RequestStatus): StoredDataRequest {
        throwResourceNotFoundExceptionIfDataRequestIdUnknown(dataRequestId)
        var dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        dataRequestLogger.logMessageForPatchingRequestStatus(dataRequestEntity.dataRequestId, requestStatus)
        dataRequestEntity.requestStatus = requestStatus
        dataRequestRepository.save(dataRequestEntity)
        dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        return utils.buildStoredDataRequestFromDataRequestEntity(dataRequestEntity)
    }
}
