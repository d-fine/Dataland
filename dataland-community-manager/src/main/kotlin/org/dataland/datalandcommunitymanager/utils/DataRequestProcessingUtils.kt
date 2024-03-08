package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.repositories.MessageRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Class holding utility functions used by the both the bulk and the single data request manager
 */
@Service
class DataRequestProcessingUtils(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val messageRepository: MessageRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val companyApi: CompanyDataControllerApi,
) {
    /**
     * We want to avoid users from using other authentication methods than jwt-authentication, such as
     * api-key-authentication.
     */
    fun throwExceptionIfNotJwtAuth() {
        if (DatalandAuthentication.fromContext() !is DatalandJwtAuthentication) {
            throw AuthenticationMethodNotSupportedException()
        }
    }

    /**
     * Returns the ID of the company corresponding to a provided identifier value, else null if none is found
     * @param identifierValue the identifier value
     * @return the company ID or null
     */
    fun getDatalandCompanyIdForIdentifierValue(identifierValue: String): String? {
        val matchingCompanyIdsAndNamesOnDataland =
            companyApi.getCompaniesBySearchString(identifierValue)
        val datalandCompanyId = if (matchingCompanyIdsAndNamesOnDataland.size == 1) {
            matchingCompanyIdsAndNamesOnDataland.first().companyId
        } else if (matchingCompanyIdsAndNamesOnDataland.size > 1) {
            throw InvalidInputApiException(
                summary = "No unique identifier. Multiple companies could be found.",
                message = "Multiple companies have been found for the identifier you specified.",
            )
        } else {
            null
        }
        dataRequestLogger
            .logMessageWhenCrossReferencingIdentifierValueWithDatalandCompanyId(identifierValue, datalandCompanyId)
        return datalandCompanyId
    }

    /**
     * Stores a DataRequestEntity from all necessary parameters
     * @param datalandCompanyId the companyID in Dataland
     * @param dataType the enum entry corresponding to the framework
     * @param reportingPeriod the reporting period
     * @param contacts a list of email addresses to inform about the potentially stored data request
     * @param message a message to equip the notification with
     */
    fun storeDataRequestEntityAsOpen(
        datalandCompanyId: String,
        dataType: DataTypeEnum,
        reportingPeriod: String,
        contacts: Set<String>? = null,
        message: String? = null,
    ): DataRequestEntity {
        val dataRequestEntity = DataRequestEntity(
            DatalandAuthentication.fromContext().userId,
            dataType.value,
            reportingPeriod,
            datalandCompanyId,
            Instant.now().toEpochMilli(),
        )
        dataRequestEntity.requestStatus = RequestStatus.Open
        dataRequestRepository.save(dataRequestEntity)
        if (!contacts.isNullOrEmpty()) {
            val messageHistory = listOf(StoredDataRequestMessageObject(contacts, message, Instant.now().toEpochMilli()))
            dataRequestEntity.associateMessages(messageHistory)
            messageRepository.saveAllAndFlush(dataRequestEntity.messageHistory)
        }
        dataRequestLogger.logMessageForStoringDataRequest(dataRequestEntity.dataRequestId)
        return dataRequestEntity
    }

    /**
     * Retrieves the data requests already existing on Dataland for the provided specifications and the current user
     * @param companyId the company ID of the data requests
     * @param framework the framework of the data requests
     * @param reportingPeriod the reporting period of the data requests
     * @param requestStatus the status of the data request
     * @return a list of the found data requests, or null if none was found
     */
    fun findAlreadyExistingDataRequestForCurrentUser(
        companyId: String,
        framework: DataTypeEnum,
        reportingPeriod: String,
        requestStatus: RequestStatus,
    ): List<DataRequestEntity>? {
        val requestingUserId = DatalandAuthentication.fromContext().userId
        val foundRequests = dataRequestRepository
            .findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriodAndRequestStatus(
                requestingUserId, companyId, framework.name, reportingPeriod, requestStatus,
            )
        if (foundRequests != null) {
            dataRequestLogger.logMessageForCheckingIfDataRequestAlreadyExists(
                companyId,
                framework,
                reportingPeriod,
                requestStatus,
            )
        }
        return foundRequests
    }

    /**
     * Checks whether a request already exists on Dataland in a non-final status (i.e. in status "Open" or "Answered"
     * @param companyId the company ID of the data request
     * @param framework the framework of the data request
     * @param reportingPeriod the reporting period of the data request
     * @return true if the data request already exists for the current user, false otherwise
     */
    fun existsDataRequestWithNonFinalStatus(
        companyId: String,
        framework: DataTypeEnum,
        reportingPeriod: String,
    ): Boolean {
        val openDataRequests = findAlreadyExistingDataRequestForCurrentUser(
            companyId, framework, reportingPeriod, RequestStatus.Open,
        )
        val answeredDataRequests = findAlreadyExistingDataRequestForCurrentUser(
            companyId, framework, reportingPeriod, RequestStatus.Answered,
        )
        if (openDataRequests.isNullOrEmpty() && answeredDataRequests.isNullOrEmpty()) {
            return false
        } else {
            if (openDataRequests != null && openDataRequests.size > 1) {
                throw ConflictApiException(
                    "More than one open data request.",
                    "There seems to be more than one open data request with the same specifications.",
                )
            }
            if (answeredDataRequests != null && answeredDataRequests.size > 1) {
                throw ConflictApiException(
                    "More than one answered data request.",
                    "There seems to be more than one answered data request with the same specifications.",
                )
            }
            return true
        }
    }
}

/**
 * Finds the entry in the DataType enum corresponding to a provided framework name
 * @param frameworkName the name of the framework
 * @return the corresponding enum entry
 */
fun getDataTypeEnumForFrameworkName(frameworkName: String): DataTypeEnum? {
    return DataTypeEnum.entries.find { it.value == frameworkName }
}
