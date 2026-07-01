package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataAvailabilityApi
import org.dataland.datalandbackend.model.DataDimensionSearchRequest
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
    override fun filterViewableDimensions(dimensions: List<BasicDataDimensions>): ResponseEntity<List<BasicDataDimensions>> =
        ResponseEntity.ok(
            if (dimensions.isEmpty()) {
                emptyList()
            } else {
                dataAvailabilityChecker.filterViewableDimensions(dimensions)
            },
        )

    override fun searchViewableDimensions(request: DataDimensionSearchRequest): ResponseEntity<List<BasicDataDimensions>> {
        val dimensionsQuery = request.toDataDimensionQuery()
        if (dimensionsQuery.isEmpty()) {
            throw InvalidInputApiException(
                "This search request must not be empty!",
                "At least one of the fields must be provided and not empty.",
            )
        }
        return ResponseEntity.ok(dataAvailabilityChecker.searchViewableDimensions(request.toDataDimensionQuery()))
    }
}
