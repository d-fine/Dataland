package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandcommunitymanager.api.RequestApi
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.services.DataRequestManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the requests endpoint
 * @param dataRequestManager service for all operations concerning the processing of data requests
 */

@RestController
class RequestController(
    @Autowired private val dataRequestManager: DataRequestManager,
) : RequestApi {

    override fun postBulkDataRequest(bulkDataRequest: BulkDataRequest): ResponseEntity<BulkDataRequestResponse> {
        return ResponseEntity.ok(
            dataRequestManager.processBulkDataRequest(bulkDataRequest),
        )
    }

    override fun getDataRequestsForUser(): ResponseEntity<List<DataRequestEntity>> {
        return ResponseEntity.ok(
            dataRequestManager.getDataRequestsForUser(),
        )
    }
}
