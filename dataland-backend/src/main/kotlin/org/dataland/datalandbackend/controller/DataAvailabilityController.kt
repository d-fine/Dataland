package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataAvailabilityApi
import org.dataland.datalandbackend.model.DataDimensionFilter
import org.dataland.datalandbackend.model.dataavailability.DataAvailabilitySearchRequest
import org.dataland.datalandbackend.services.DataAvailabilityChecker
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for data availability endpoints.
 * @param dataAvailabilityChecker the service used to determine data availability
 */
@RestController
class DataAvailabilityController(
    @Autowired private val dataAvailabilityChecker: DataAvailabilityChecker,
) : DataAvailabilityApi {
    override fun getActiveDimensions(dimensions: List<BasicDataDimensions>): ResponseEntity<List<BasicDataDimensions>> =
        ResponseEntity.ok(dataAvailabilityChecker.getAvailableDimensions(dimensions))

    override fun getAvailableDataDimensions(request: DataAvailabilitySearchRequest): ResponseEntity<List<BasicDataDimensions>> {
        if (request.frameworksOrDataPointTypes.isEmpty()) {
            throw InvalidInputApiException(
                "frameworksOrDataPointTypes must not be empty.",
                "The request body must contain at least one frameworkOrDataPointType.",
            )
        }
        return ResponseEntity.ok(
            dataAvailabilityChecker.getAvailableDimensions(
                DataDimensionFilter(
                    companyIds = request.companyIds.ifEmpty { null },
                    dataTypes = request.frameworksOrDataPointTypes,
                    reportingPeriods = request.reportingPeriods.ifEmpty { null },
                ),
            ),
        )
    }
}
