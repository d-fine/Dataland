package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandcommunitymanager.api.RequestApi
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.services.RequestManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the requests endpoint
 * @param requestManager the request manager service for all operations concerning the processing of data requests
 */

@RestController
class RequestController(
    @Autowired private val requestManager: RequestManager,
    // @Autowired private val emailSender: EmailSender,
    // @Autowired private val communityRepositoryInterface: CommunityRepository,
) : RequestApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun postBulkDataRequest(bulkDataRequest: BulkDataRequest): ResponseEntity<BulkDataRequestResponse> {
        // logger.info("Received a bulk data request.") TODO
        return ResponseEntity.ok(
            requestManager.processBulkDataRequest(bulkDataRequest),
        )
    }

    override fun getDataRequestsForUser(): ResponseEntity<List<DataRequestEntity>> {
        return ResponseEntity.ok(
            requestManager.getDataRequestsForUser(),
        )
    }
}
