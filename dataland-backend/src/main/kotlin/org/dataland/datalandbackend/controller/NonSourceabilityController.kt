package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.NonSourceabilityApi
import org.dataland.datalandbackend.model.DataDimensionSearchRequest
import org.dataland.datalandbackend.services.NonSourceabilityInformationManager
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for bulk non-sourceability query endpoints.
 * @param nonSourceabilityInformationManager the service used to look up non-sourceability information
 */
@RestController
class NonSourceabilityController
    @Autowired
    constructor(
        private val nonSourceabilityInformationManager: NonSourceabilityInformationManager,
    ) : NonSourceabilityApi {
        private val logger = LoggerFactory.getLogger(javaClass)

        override fun searchNonSourceableDimensions(request: DataDimensionSearchRequest): ResponseEntity<Set<BasicDataDimensions>> {
            logger.info("Received a request to search non-sourceable dimensions with the search request being $request")
            val query = request.toDataDimensionQuery()
            return ResponseEntity.ok(nonSourceabilityInformationManager.searchActiveNonSourceableDimensions(query))
        }
    }
