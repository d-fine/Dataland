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
            for (dataDimension in listOfRequestedDataDimensionTuples) {
                dataSourcingValidator.validateAndGetCompanyIdForIdentifier(dataDimension.companyId)
            }

            return BulkDataRequestResponse(
                acceptedDataRequests = emptyList(),
                rejectedDataRequests = emptyList(),
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
