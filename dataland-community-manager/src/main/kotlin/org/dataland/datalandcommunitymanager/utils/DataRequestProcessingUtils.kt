package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestStatusObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.DataRequestHistoryManager
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.jvm.optionals.getOrElse

/**
 * Class holding utility functions used by the both the bulk and the single data request manager
 */
@Service
class DataRequestProcessingUtils(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestHistoryManager: DataRequestHistoryManager,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val companyApi: CompanyDataControllerApi,
    @Autowired private val metaDataApi: MetaDataControllerApi,
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
     * @param returnOnlyUnique boolean if only unique matches should be returned (null if not unique) or if an
     * exception will be thrown (default)
     * @return the company ID or null
     */
    fun getDatalandCompanyIdAndNameForIdentifierValue(
        identifierValue: String,
        returnOnlyUnique: Boolean = false,
    ): CompanyIdAndName? {
        val matchingCompanyIdsAndNamesOnDataland =
            companyApi.getCompaniesBySearchString(identifierValue)
        val datalandCompanyIdAndName = if (matchingCompanyIdsAndNamesOnDataland.size == 1) {
            matchingCompanyIdsAndNamesOnDataland.first()
        } else if (matchingCompanyIdsAndNamesOnDataland.size > 1 && !returnOnlyUnique) {
            throw InvalidInputApiException(
                summary = "No unique identifier. Multiple companies could be found.",
                message = "Multiple companies have been found for the identifier you specified.",
            )
        } else {
            null
        }
        dataRequestLogger
            .logMessageWhenCrossReferencingIdentifierValueWithDatalandCompanyId(
                identifierValue,
                datalandCompanyIdAndName?.companyId,
            )
        return datalandCompanyIdAndName
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
        val creationTime = Instant.now().toEpochMilli()

        val dataRequestEntity = DataRequestEntity(
            DatalandAuthentication.fromContext().userId,
            dataType.value,
            reportingPeriod,
            datalandCompanyId,
            creationTime,
        )
        dataRequestRepository.save(dataRequestEntity)

        val requestStatusObject = listOf(StoredDataRequestStatusObject(RequestStatus.Open, creationTime, null))
        dataRequestEntity.associateRequestStatus(requestStatusObject)
        dataRequestHistoryManager.saveStatusHistory(dataRequestEntity.dataRequestStatusHistory)

        if (!contacts.isNullOrEmpty()) {
            val messageHistory = listOf(StoredDataRequestMessageObject(contacts, message, creationTime))
            dataRequestEntity.associateMessages(messageHistory)
            dataRequestHistoryManager.saveMessageHistory(dataRequestEntity.messageHistory)
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
            .findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                requestingUserId, companyId, framework.name, reportingPeriod,
            )?.filter {
            it.requestStatus == requestStatus
        }
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
        return if (openDataRequests.isNullOrEmpty() && answeredDataRequests.isNullOrEmpty()) {
            false
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
            true
        }
    }

    /**
     * This method checks if a dataset exists for the specified parameters
     * @param companyId the dataland companyId of the company from which data is requested
     * @param reportingPeriod the reportingPeriod for which data is requested
     * @param dataType the framework dataType for which data is requested
     */
    fun matchingDatasetExists(companyId: String, reportingPeriod: String, dataType: DataTypeEnum): Boolean {
        val matchingDatasets = metaDataApi.getListOfDataMetaInfo(
            companyId = companyId,
            dataType = dataType,
            showOnlyActive = true,
            reportingPeriod = reportingPeriod,
        )
        return matchingDatasets.isNotEmpty()
    }

    /**
     * This method finds all DataRequestsEntity for a specified dataset that have a specific accessStatus.
     * @param companyId the companyId for which the access status should be checked
     * @param reportingPeriod the reportingPeriod for which the access status should be checked
     * @param dataType the framework dataType for which the access status should be checked
     * @param userId the userId for which the access status should be checked
     * @param accessStatus the accessStatus for which the check should be conducted
     */
    fun findRequestsByAccessStatus(
        companyId: String,
        reportingPeriod: String,
        dataType: DataTypeEnum,
        userId: String,
        accessStatus: AccessStatus,
    ): List<DataRequestEntity>? {
        val foundAccess = dataRequestRepository
            .findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                userId, companyId, dataType.name, reportingPeriod,
            )?.filter {
            it.accessStatus == accessStatus
        }
        if (foundAccess != null) {
            dataRequestLogger.logMessageForCheckingIfUserHasAccessToDataset(
                companyId,
                dataType,
                reportingPeriod,
                accessStatus,
            )
        }
        // TODO Test if this only checks the toplevel value of accessStatus or if this goes through the requestHistory
        //  and checks if there was at least on Granted accessStatus
        // TODO if it his the second case logic has to be adapted
        // TODO name of this method is not quite right in regards to the inputs
        return foundAccess
    }

    /**
     * This method checks if the requesting user has granted access to the information for a specific company,
     * reporting period and data type
     * @param companyId the companyId for which the access status should be checked
     * @param reportingPeriod the reportingPeriod for which the access status should be checked
     * @param dataType the framework dataType for which the access status should be checked
     * @param userId the userId for which the access status should be checked
     */
    fun hasAccessToPrivateDataset(
        companyId: String,
        reportingPeriod: String,
        dataType: DataTypeEnum,
        userId: String,
    ): Boolean {
        return !findRequestsByAccessStatus(companyId, reportingPeriod, dataType, userId, AccessStatus.Granted)
            .isNullOrEmpty()
    }

    /**
     * This method is used to request access to a private dataset
     * @param userId the userId of the user requesting access to the dataset
     * @param companyId the companyId of the company to which the dataset belongs
     * @param dataType the datatype of the dataset to which access was requested
     * @param reportingPeriod the reportingPeriod of the dataset to which access was requested
     */
    fun createAccessRequestToPrivateDataset(
        userId: String,
        companyId: String,
        dataType: DataTypeEnum,
        reportingPeriod: String,
    ) {
        val existingRequestsOfUser = dataRequestRepository
            .findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                userId, companyId, dataType.name, reportingPeriod,
            )
        if (!existingRequestsOfUser.isNullOrEmpty()) {
            val dataRequestId = existingRequestsOfUser[0].dataRequestId
            val dataRequestEntity = dataRequestRepository.findById(dataRequestId).getOrElse {
                throw DataRequestNotFoundApiException(dataRequestId)
            }
            val modificationTime = Instant.now().toEpochMilli()
            dataRequestEntity.lastModifiedDate = modificationTime
            dataRequestRepository.save(dataRequestEntity)
            if (dataRequestEntity.accessStatus == null || dataRequestEntity.accessStatus == AccessStatus.Declined) {
                val requestStatusObject = listOf(
                    StoredDataRequestStatusObject(
                        dataRequestEntity.requestStatus, modificationTime,
                        AccessStatus.Pending,
                    ),
                )

                dataRequestEntity.associateRequestStatus(requestStatusObject)
                dataRequestHistoryManager.saveStatusHistory(dataRequestEntity.dataRequestStatusHistory)
                dataRequestLogger.logMessageForPatchingAccessStatus(dataRequestId, AccessStatus.Pending)
            }
        }
        // TODO
        // Request identifizieren, access status patchen
        // Nur neuen excess request erstellen wenn Staus auf null oder Declined ist, Access und Pending raus filtern
    }
}

/**
 * Finds the entry in the DataType enum corresponding to a provided framework name
 * @param frameworkName the name of the framework
 * @return the corresponding enum entry
 */
fun getDataTypeEnumForFrameworkName(frameworkName: String): DataTypeEnum? {
    return DataTypeEnum.entries.find { it.value == frameworkName }
    // TODO Why is this function not part of the class?
}
