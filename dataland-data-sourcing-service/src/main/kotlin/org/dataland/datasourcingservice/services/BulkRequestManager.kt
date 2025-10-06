package org.dataland.datasourcingservice.services

import org.dataland.datalandbackend.openApiClient.model.BasicDataDimensions
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datasourcingservice.model.request.BulkDataRequest
import org.dataland.datasourcingservice.model.request.BulkDataRequestResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service class that manages all operations related to bulk requests.
 */
@Service("BulkRequestManager")
class BulkRequestManager
    @Autowired
    constructor(
        private val dataSourcingValidator: DataSourcingValidator,
    ) {
        /**
         * Processes a bulk data request from a user
         * @param bulkDataRequest info provided by a user in order to request a bulk of datasets on Dataland.
         * @return relevant info to the user as a response after posting a bulk data request
         */
        @Transactional
        fun processBulkDataRequest(bulkDataRequest: BulkDataRequest): BulkDataRequestResponse {
            assertNoEmptyListsInBulkRequest(bulkDataRequest)
            val listOfRequestedDataDimensionTuples = generateCartesianProduct(bulkDataRequest)

            val validatedResults =
                listOfRequestedDataDimensionTuples.map { dataDimension ->
                    val validationResult = dataSourcingValidator.validateAndGetCompanyId(dataDimension.companyId)
                    dataDimension to validationResult // Pair the data dimension with its validation result
                }

            val (accepted, rejected) =
                validatedResults.partition { (_, result) ->
                    result.isSuccess
                }

            val acceptedRequests =
                accepted.map { (dataDimension, result) ->
                    val validatedId = result.getOrThrow() // Extract the validated company ID
                    dataDimension.copy(companyId = validatedId.toString()) // Update companyId
                }

            val rejectedRequests =
                rejected.map { (dataDimension, _) ->
                    dataDimension // Extract rejected requests without modification
                }

            return BulkDataRequestResponse(
                acceptedDataRequests = acceptedRequests,
                rejectedDataRequests = rejectedRequests,
            )
        }

        private fun assertNoEmptyListsInBulkRequest(bulkDataRequest: BulkDataRequest) {
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
            bulkDataRequest.companyIdentifiers.flatMap { companyId ->
                bulkDataRequest.dataTypes.flatMap { dataType ->
                    bulkDataRequest.reportingPeriods.map { period ->
                        BasicDataDimensions(
                            companyId = companyId,
                            dataType = dataType.toString(),
                            reportingPeriod = period,
                        )
                    }
                }
            }
    }
