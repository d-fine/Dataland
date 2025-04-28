package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestStatusObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.repositories.MessageRepository
import org.dataland.datalandcommunitymanager.repositories.RequestStatusRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Class holding utility functions used by the both the bulk and the single data request manager
 */
@Service
class DataRequestProcessingUtils
    @Suppress("LongParameterList")
    @Autowired
    constructor(
        private val dataRequestRepository: DataRequestRepository,
        private var requestStatusRepository: RequestStatusRepository,
        private var messageRepository: MessageRepository,
        private val dataRequestLogger: DataRequestLogger,
        private val companyApi: CompanyDataControllerApi,
        private val metaDataApi: MetaDataControllerApi,
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
         * Validates provided company identifiers by querying the backend.
         * @param identifiers the identifiers to validate
         * @return a pair of a map of valid identifiers to company ID and name and a list of invalid identifiers
         */
        fun performIdentifierValidation(identifiers: List<String>): Pair<Map<String, CompanyIdAndName>, List<String>> {
            val validationResults = companyApi.postCompanyValidation(identifiers)
            val validIdentifiers = mutableMapOf<String, CompanyIdAndName>()
            val invalidIdentifiers = validationResults.filter { it.companyInformation == null }.map { it.identifier }
            validationResults.filter { it.companyInformation != null }.forEach {
                validIdentifiers[it.identifier] =
                    CompanyIdAndName(
                        companyName = it.companyInformation?.companyName ?: "",
                        companyId = it.companyInformation?.companyId ?: "",
                    )
            }

            return Pair(validIdentifiers, invalidIdentifiers)
        }

        /**
         * Stores a DataRequestEntity from all necessary parameters
         * @param datalandCompanyId the companyID in Dataland
         * @param dataType the enum entry corresponding to the framework
         * @param reportingPeriod the reporting period
         * @param contacts a list of email addresses to inform about the potentially stored data request
         * @param message a message to equip the notification with
         */
        @Suppress("complexity:LongParameterList")
        fun storeDataRequestEntityAsOpen(
            userId: String,
            datalandCompanyId: String,
            dataType: DataTypeEnum,
            notifyMeImmediately: Boolean,
            reportingPeriod: String,
            contacts: Set<String>? = null,
            message: String? = null,
        ): DataRequestEntity {
            val creationTime = Instant.now().toEpochMilli()

            val dataRequestEntity =
                DataRequestEntity(
                    userId,
                    dataType.value,
                    notifyMeImmediately,
                    reportingPeriod,
                    datalandCompanyId,
                    creationTime,
                )
            dataRequestRepository.save(dataRequestEntity)
            val accessStatus =
                if (dataType == DataTypeEnum.vsme) {
                    AccessStatus.Pending
                } else {
                    AccessStatus.Public
                }
            addNewRequestStatusToHistory(
                dataRequestEntity, RequestStatus.Open, accessStatus, null, creationTime,
            )

            if (!contacts.isNullOrEmpty()) {
                addMessageToMessageHistory(dataRequestEntity, contacts, message, creationTime)
            }
            dataRequestLogger.logMessageForStoringDataRequest(dataRequestEntity.dataRequestId)

            return dataRequestEntity
        }

        /**
         * For a given dataRequestEntity, this function adds and persists a new entry to
         * the messageHistory of the dataRequestEntity.
         * The new entry contains a set of contacts, an optional message and a modificationTime.
         * This function should be called within a transaction.
         */
        fun addMessageToMessageHistory(
            dataRequestEntity: DataRequestEntity,
            contacts: Set<String>,
            message: String?,
            modificationTime: Long,
        ) {
            val requestMessageObject = StoredDataRequestMessageObject(contacts, message, modificationTime)
            val requestMessageEntity = MessageEntity(requestMessageObject, dataRequestEntity)
            messageRepository.save(requestMessageEntity)
            dataRequestEntity.addRequestEventToMessageHistory(requestMessageEntity)
        }

        /**
         * For a given dataRequestEntity, this function adds and persists a new entry to
         * the requestStatusHistory of the dataRequestEntity.
         * The new entry contains a requestStatus, an accessStatus and a modificationTime.
         * This function should be called within a transaction.
         */
        fun addNewRequestStatusToHistory(
            dataRequestEntity: DataRequestEntity,
            requestStatus: RequestStatus,
            accessStatus: AccessStatus,
            requestStatusChangeReason: String?,
            modificationTime: Long,
            answeringDataId: String? = null,
        ) {
            val requestStatusObject =
                StoredDataRequestStatusObject(
                    requestStatus, modificationTime, accessStatus, requestStatusChangeReason, answeringDataId,
                )
            val requestStatusEntity = RequestStatusEntity(requestStatusObject, dataRequestEntity)

            requestStatusRepository.save(requestStatusEntity)
            dataRequestEntity.addToDataRequestStatusHistory(requestStatusEntity)
        }

        /**
         * Retrieves the data requests already existing on Dataland for the provided specifications and the current user
         * @param companyId the company ID of the data requests
         * @param framework the framework of the data requests
         * @param reportingPeriod the reporting period of the data requests
         * @param requestStatus the status of the data request
         * @return a list of the found data requests, or null if none was found
         */
        fun findAlreadyExistingDataRequestForUser(
            userId: String,
            companyId: String,
            framework: DataTypeEnum,
            reportingPeriod: String,
            requestStatus: RequestStatus,
        ): List<DataRequestEntity>? {
            val foundRequests =
                dataRequestRepository
                    .findByUserIdAndDatalandCompanyIdAndDataTypeAndReportingPeriod(
                        userId, companyId, framework.value, reportingPeriod,
                    )?.filter {
                        it.requestStatus == requestStatus
                    }
            if (!foundRequests.isNullOrEmpty()) {
                dataRequestLogger.logMessageForCheckingIfDataRequestAlreadyExists(
                    userId,
                    companyId,
                    framework,
                    reportingPeriod,
                    requestStatus,
                )
            }
            return foundRequests
        }

        /**
         * Checks whether a request already exists on Dataland in a non-final status (i.e. in status "Open" or "Answered")
         * @param companyId the company ID of the data request
         * @param framework the framework of the data request
         * @param reportingPeriod the reporting period of the data request
         * @return true if the data request already exists for the current user, false otherwise
         */
        fun existsDataRequestWithNonFinalStatus(
            companyId: String,
            framework: DataTypeEnum,
            reportingPeriod: String,
            userId: String,
        ): Boolean {
            val openDataRequests =
                findAlreadyExistingDataRequestForUser(
                    userId, companyId, framework, reportingPeriod, RequestStatus.Open,
                )
            val answeredDataRequests =
                findAlreadyExistingDataRequestForUser(
                    userId, companyId, framework, reportingPeriod, RequestStatus.Answered,
                )
            return !(openDataRequests.isNullOrEmpty() && answeredDataRequests.isNullOrEmpty())
        }

        /**
         * Checks whether a request already exists on Dataland in a non-final status (i.e. in status "Open" or "Answered")
         * and returns the request id
         * @param companyId the company ID of the data request
         * @param framework the framework of the data request
         * @param reportingPeriod the reporting period of the data request
         * @return the requestId if a request in non-final status exists, else null
         */
        fun getRequestIdForDataRequestWithNonFinalStatus(
            companyId: String,
            framework: DataTypeEnum,
            reportingPeriod: String,
        ): String? {
            val foundRequests = mutableListOf<DataRequestEntity>()
            val userId = DatalandAuthentication.fromContext().userId
            findAlreadyExistingDataRequestForUser(
                userId, companyId, framework, reportingPeriod, RequestStatus.Open,
            )?.forEach { foundRequests.add(it) }

            findAlreadyExistingDataRequestForUser(
                userId, companyId, framework, reportingPeriod, RequestStatus.Answered,
            )?.forEach { foundRequests.add(it) }

            return foundRequests.firstOrNull()?.dataRequestId
        }

        /**
         * This method checks if a dataset exists for the specified parameters
         * @param companyId the dataland companyId of the company from which data is requested
         * @param reportingPeriod the reportingPeriod for which data is requested
         * @param dataType the framework dataType for which data is requested
         * @return true if matching datasets were found
         */
        fun matchingDatasetExists(
            companyId: String,
            reportingPeriod: String,
            dataType: DataTypeEnum,
        ): Boolean {
            val matchingDatasets =
                metaDataApi.getListOfDataMetaInfo(
                    companyId = companyId,
                    dataType = dataType,
                    showOnlyActive = true,
                    reportingPeriod = reportingPeriod,
                )
            return matchingDatasets.isNotEmpty()
        }

        /**
         * Finds the entry in the DataType enum corresponding to a provided framework name
         * @param frameworkName the name of the framework
         * @return the corresponding enum entry
         */
        fun getDataTypeEnumForFrameworkName(frameworkName: String): DataTypeEnum? = DataTypeEnum.entries.find { it.value == frameworkName }
    }
