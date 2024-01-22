package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.api.RequestApi
import org.dataland.datalandcommunitymanager.model.dataRequest.*
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

    override fun getDataRequestsForUser(): ResponseEntity<List<StoredDataRequest>> {
        return ResponseEntity.ok(
            dataRequestManager.getDataRequestsForUser(),
        )
    }

    override fun getAggregatedDataRequests(
        identifierValue: String?,
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriod: String?,
    ): List<AggregatedDataRequest> {
        return dataRequestManager.getAggregatedDataRequests(identifierValue, dataTypes, reportingPeriod)
    }

    override fun patchDataRequest(dataRequestId: String, requestStatus: RequestStatus): ResponseEntity<StoredDataRequest> {
        return ResponseEntity.ok(dataRequestManager.patchDataRequest(dataRequestId, requestStatus))
    }
}
