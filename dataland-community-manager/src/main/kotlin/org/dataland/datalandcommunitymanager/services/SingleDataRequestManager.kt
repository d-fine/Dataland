package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
    val logger = LoggerFactory.getLogger(javaClass)
    private val utils = DataRequestManagerUtils(dataRequestRepository, dataRequestLogger, companyGetter, objectMapper)

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
        val storedDataRequests = mutableListOf<StoredDataRequest>()
        logger.info("The datalandCompanyId is: $singleDataRequest.companyIdentifier")
        val datalandCompanyId = utils.getDatalandCompanyIdForIdentifierValue(
            singleDataRequest.companyIdentifier,
        )
        logger.info("The datalandCompanyId is: $datalandCompanyId")
        if (datalandCompanyId == null) {
            throwInvalidInputApiExceptionBecauseAllIdentifiersRejected()
        } else {
            throwInvalidInputApiExceptionIfFinalMessageObjectNotMeaningful(singleDataRequest)
            storeDataRequestsAndAddThemToListForEachReportingPeriodIfNotAlreadyExisting(
                storedDataRequests, singleDataRequest, datalandCompanyId,
            )
            singleDataRequestEmailSender.sendSingleDataRequestEmails(
                userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
                singleDataRequest = singleDataRequest,
                datalandCompanyId,
            )
        }
        return storedDataRequests
    }

    private fun throwInvalidInputApiExceptionIfFinalMessageObjectNotMeaningful(singleDataRequest: SingleDataRequest) {
        if (utils.isContactListTrivial(singleDataRequest.contactList) && !singleDataRequest.message.isNullOrBlank()) {
            throw InvalidInputApiException(
                "Insufficient information to create message object.",
                "Without at least one proper email address being provided no message can be forwarded.",
            )
        }
    }

    private fun storeDataRequestsAndAddThemToListForEachReportingPeriodIfNotAlreadyExisting(
        storedDataRequests: MutableList<StoredDataRequest>,
        singleDataRequest: SingleDataRequest,
        datalandCompanyId: String,
    ) {
        for (reportingPeriod in singleDataRequest.listOfReportingPeriods.distinct()) {
            storedDataRequests.add(
                utils.buildStoredDataRequestFromDataRequestEntity(
                    utils.storeDataRequestEntityIfNotExisting(
                        datalandCompanyId,
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
        datalandCompanyId: String?,
    ): List<StoredDataRequest>? {
        val filter = GetDataRequestsSearchFilter(
            dataTypeNameFilter = dataType?.name ?: "",
            userIdFilter = userId ?: "",
            requestStatus = requestStatus,
            reportingPeriodFilter = reportingPeriod ?: "",
            datalandCompanyIdFilter = datalandCompanyId ?: "",
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
    private fun throwInvalidInputApiExceptionBecauseAllIdentifiersRejected() {
        val summary = "The provided company identifier has an invalid format."
        val message = "The company identifier you provided do not match the patterns " +
            "of a valid LEI, ISIN or PermId."
        throw InvalidInputApiException(
            summary,
            message,
        )
    }
}
