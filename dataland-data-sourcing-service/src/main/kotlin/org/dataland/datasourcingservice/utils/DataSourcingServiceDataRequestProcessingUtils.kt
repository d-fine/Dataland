package org.dataland.datasourcingservice.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import java.util.UUID

/**
 * Utility service class for processing data requests in the Data Sourcing Service.
 */
@Service("DataSourcingServiceDataRequestProcessingUtils")
class DataSourcingServiceDataRequestProcessingUtils
    @Autowired
    constructor(
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val requestLogger: RequestLogger,
        private val requestRepository: RequestRepository,
    ) {
        /**
         * Validates provided company identifiers by querying the backend.
         * @param identifiers the identifiers to validate
         * @return a pair of a map of valid identifiers to company ID and name and a list of invalid identifiers
         */
        fun performIdentifierValidation(identifiers: List<String>): Pair<Map<String, CompanyIdAndName>, List<String>> {
            val validationResults = companyDataControllerApi.postCompanyValidation(identifiers)
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
         * Retrieves the data requests already existing on Dataland for the provided specifications and the current user
         * @param companyId the company ID of the data requests
         * @param framework the framework of the data requests
         * @param reportingPeriod the reporting period of the data requests
         * @param requestState the state of the data request
         * @return a list of the found data requests, or null if none was found
         */
        fun findAlreadyExistingDataRequestsForUser(
            userId: UUID,
            companyId: String,
            framework: String,
            reportingPeriod: String,
            requestState: RequestState,
        ): List<RequestEntity> {
            val foundRequests =
                requestRepository
                    .findByUserIdAndCompanyIdAndDataTypeAndReportingPeriod(
                        userId, companyId, framework, reportingPeriod,
                    ).filter {
                        it.state == requestState
                    }
            if (!foundRequests.isEmpty()) {
                requestLogger.logMessageForCheckingIfDataRequestAlreadyExists(
                    userId,
                    companyId,
                    framework,
                    reportingPeriod,
                    requestState,
                )
            }
            return foundRequests
        }

        /**
         * Retrieves the data requests already existing on Dataland for the provided specifications and the current user
         * that are not in a final state (i.e. Open or Processing)
         * @param companyId the company ID of the data requests
         * @param framework the framework of the data requests
         * @param reportingPeriod the reporting period of the data requests
         * @return a list of the found data requests, or null if none was found
         */
        fun getExistingDataRequestsWithNonFinalState(
            companyId: String,
            framework: String,
            reportingPeriod: String,
            userId: UUID,
        ): List<RequestEntity> {
            val openDataRequests =
                findAlreadyExistingDataRequestsForUser(
                    userId, companyId, framework, reportingPeriod, RequestState.Open,
                )
            val dataRequestsInProcess =
                findAlreadyExistingDataRequestsForUser(
                    userId, companyId, framework, reportingPeriod, RequestState.Processing,
                )
            return openDataRequests + dataRequestsInProcess
        }

        /**
         * Stores a DataRequestEntity from all necessary parameters
         * @param userId the userID in Dataland
         * @param companyId the companyID in Dataland
         * @param dataType the framework name string corresponding to the data request
         * @param reportingPeriod the reporting period
         * @return the stored DataRequestEntity
         */
        fun storeRequestEntityAsOpen(
            userId: UUID,
            companyId: String,
            dataType: String,
            reportingPeriod: String,
        ): RequestEntity {
            val creationTimestamp = Date.from(Instant.now())

            val dataRequestEntity =
                RequestEntity(
                    userId,
                    companyId,
                    dataType,
                    reportingPeriod,
                    creationTimestamp,
                )

            requestRepository.save(dataRequestEntity)

            requestLogger.logMessageForStoringDataRequest(dataRequestEntity.id)

            return dataRequestEntity
        }
    }
