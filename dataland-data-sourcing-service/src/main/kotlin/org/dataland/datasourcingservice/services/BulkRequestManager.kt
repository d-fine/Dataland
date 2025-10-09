package org.dataland.datasourcingservice.services

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicDataDimensions
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.request.BulkDataRequest
import org.dataland.datasourcingservice.model.request.BulkDataRequestResponse
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service class that manages all operations related to bulk requests.
 */
@Service("BulkRequestManager")
class BulkRequestManager
    @Autowired
    constructor(
        private val dataSourcingValidator: DataSourcingValidator,
        private val requestCreationService: RequestCreationService,
        private val metaDataController: MetaDataControllerApi,
        @PersistenceContext private val entityManager: EntityManager,
    ) {
        /**
         * Processes a bulk data request from a user
         * @param bulkDataRequest info provided by a user in order to request a bulk of datasets on Dataland.
         * @return relevant info to the user as a response after posting a bulk data request
         */
        @Transactional
        fun processBulkDataRequest(
            bulkDataRequest: BulkDataRequest,
            userId: UUID?,
        ): BulkDataRequestResponse {
            assertNoEmptySetsInBulkRequest(bulkDataRequest)
            val userIdToUse = userId ?: UUID.fromString(DatalandAuthentication.fromContext().userId)
            val listOfRequestedDataDimensionTuples = generateCartesianProduct(bulkDataRequest)

            val (validatedRequests, invalidRequests) =
                dataSourcingValidator.validateBulkDataRequest(
                    listOfRequestedDataDimensionTuples,
                )
            val existingRequests = getExistingRequests(validatedRequests, userIdToUse)
            val existingDatasets = getExistingDatasets(validatedRequests - existingRequests)

            val acceptedDataRequests = validatedRequests - existingRequests - existingDatasets

            acceptedDataRequests.forEach { dataDimension ->
                requestCreationService.storeRequest(userIdToUse, dataDimension)
            }

            return BulkDataRequestResponse(
                acceptedDataRequests = acceptedDataRequests,
                invalidDataRequests = invalidRequests,
                existingDataRequests = existingRequests,
                existingDataSets = existingDatasets,
            )
        }

        private fun getExistingDatasets(requests: List<BasicDataDimensions>): List<BasicDataDimensions> =
            metaDataController
                .retrieveMetaDataOfActiveDatasets(
                    basicDataDimensions = requests,
                ).map {
                    BasicDataDimensions(
                        companyId = it.companyId,
                        dataType = it.dataType.toString(),
                        reportingPeriod = it.reportingPeriod,
                    )
                }

        /**
         * Function to retrieve all active data requests for a user based on the provided data dimensions and the user ID.
         * For performance reasons tuple matching is used and requires a native query via entity manager.
         * JPA does not support tuple matching.
         * @param dataDimensions List of data dimensions to filter the requests.
         * @param userId The ID of the user for whom to retrieve the active requests.
         */
        private fun getExistingRequests(
            dataDimensions: List<BasicDataDimensions>,
            userId: UUID,
        ): List<BasicDataDimensions> {
            val formattedTuples =
                dataDimensions.joinToString(", ") {
                    "('${it.companyId}', '${it.dataType}', '${it.reportingPeriod}')"
                }

            val queryToExecute =
                """SELECT * FROM requests request
                WHERE (request.company_id, request.data_type, request.reporting_period) IN ($formattedTuples)
                AND request.user_id = '$userId'"""

            return if (dataDimensions.isNotEmpty()) {
                val query = entityManager.createNativeQuery(queryToExecute, RequestEntity::class.java)
                return query.resultList
                    .filterIsInstance<RequestEntity>()
                    .map {
                        BasicDataDimensions(
                            companyId = it.companyId.toString(),
                            reportingPeriod = it.reportingPeriod,
                            dataType = it.dataType,
                        )
                    }
            } else {
                emptyList()
            }
        }

        private fun assertNoEmptySetsInBulkRequest(bulkDataRequest: BulkDataRequest) {
            val identifiers = bulkDataRequest.companyIdentifiers
            val frameworks = bulkDataRequest.dataTypes
            val reportingPeriods = bulkDataRequest.reportingPeriods
            if (identifiers.isEmpty() || frameworks.isEmpty() || reportingPeriods.isEmpty()) {
                val errorMessage = "No empty lists are allowed as input for bulk data request."
                throw InvalidInputApiException(
                    errorMessage,
                    errorMessage,
                )
            }
        }

        private fun generateCartesianProduct(bulkDataRequest: BulkDataRequest): List<BasicDataDimensions> =
            bulkDataRequest.companyIdentifiers
                .flatMap { companyId ->
                    bulkDataRequest.dataTypes.flatMap { dataType ->
                        bulkDataRequest.reportingPeriods.map { period ->
                            BasicDataDimensions(
                                companyId = companyId,
                                dataType = dataType,
                                reportingPeriod = period,
                            )
                        }
                    }
                }.distinct()
    }
