package org.dataland.datasourcingservice.controller

import org.dataland.datasourcingservice.api.MixedApi
import org.dataland.datasourcingservice.model.mixed.MixedExtendedStoredRequest
import org.dataland.datasourcingservice.model.mixed.MixedRequestSearchFilter
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the requests endpoint
 */
@RestController
class MixedController
    @Autowired
    constructor(
        private val requestQueryManager: RequestQueryManager,
    ) : MixedApi {
        override fun postRequestCountQuery(mixedRequestSearchFilter: MixedRequestSearchFilter<String>): ResponseEntity<Int> =
            ResponseEntity.ok(
                requestQueryManager.getNumberOfRequests(mixedRequestSearchFilter.convertToSearchFilterWithUUIDs()),
            )

        override fun postRequestSearch(
            mixedRequestSearchFilter: MixedRequestSearchFilter<String>,
            chunkSize: Int,
            chunkIndex: Int,
        ): ResponseEntity<List<MixedExtendedStoredRequest>> =
            ResponseEntity.ok(
                requestQueryManager.searchRequests(
                    mixedRequestSearchFilter.convertToSearchFilterWithUUIDs(), chunkSize, chunkIndex,
                ),
            )

        override fun getRequestsForRequestingUser(): ResponseEntity<List<MixedExtendedStoredRequest>> =
            ResponseEntity.ok(
                requestQueryManager.getRequestsForRequestingUser(),
            )
    }
