package org.dataland.datasourcingservice.controller

import org.dataland.datasourcingservice.api.EnhancedRequestApi
import org.dataland.datasourcingservice.model.enhanced.DataSourcingEnhancedRequest
import org.dataland.datasourcingservice.model.enhanced.RequestSearchFilter
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the requests endpoint
 */
@RestController
class EnhancedRequestController
    @Autowired
    constructor(
        private val requestQueryManager: RequestQueryManager,
    ) : EnhancedRequestApi {
        override fun postRequestCountQuery(requestSearchFilter: RequestSearchFilter<String>): ResponseEntity<Int> =
            ResponseEntity.ok(
                requestQueryManager.getNumberOfRequests(requestSearchFilter.convertToSearchFilterWithUUIDs()),
            )

        override fun postRequestSearch(
            requestSearchFilter: RequestSearchFilter<String>,
            chunkSize: Int,
            chunkIndex: Int,
        ): ResponseEntity<List<DataSourcingEnhancedRequest>> =
            ResponseEntity.ok(
                requestQueryManager.searchRequests(
                    requestSearchFilter.convertToSearchFilterWithUUIDs(), chunkSize, chunkIndex,
                ),
            )

        override fun getRequestsForRequestingUser(): ResponseEntity<List<DataSourcingEnhancedRequest>> =
            ResponseEntity.ok(
                requestQueryManager.getRequestsForRequestingUser(),
            )
    }
