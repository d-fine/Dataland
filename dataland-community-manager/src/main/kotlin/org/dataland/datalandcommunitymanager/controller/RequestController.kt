package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.api.RequestApi
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.services.BulkDataRequestManager
import org.dataland.datalandcommunitymanager.services.DataRequestAlterationManager
import org.dataland.datalandcommunitymanager.services.DataRequestQueryManager
import org.dataland.datalandcommunitymanager.services.SingleDataRequestManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the requests endpoint
 * @param bulkDataRequestManager service for all operations concerning the processing of data requests
 */

@RestController
class RequestController(
    @Autowired private val bulkDataRequestManager: BulkDataRequestManager,
    @Autowired private val singleDataRequestManager: SingleDataRequestManager,
    @Autowired private val dataRequestQueryManager: DataRequestQueryManager,
    @Autowired private val dataRequestAlterationManager: DataRequestAlterationManager,
) : RequestApi {

    override fun postBulkDataRequest(bulkDataRequest: BulkDataRequest): ResponseEntity<BulkDataRequestResponse> {
        return ResponseEntity.ok(
            bulkDataRequestManager.processBulkDataRequest(bulkDataRequest),
        )
    }

    override fun getDataRequestsForUser(): ResponseEntity<List<StoredDataRequest>> {
        return ResponseEntity.ok(dataRequestQueryManager.getDataRequestsForUser())
    }

    override fun getAggregatedDataRequests(
        identifierValue: String?,
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriod: String?,
    ): ResponseEntity<List<AggregatedDataRequest>> {
        return ResponseEntity.ok(
            dataRequestQueryManager.getAggregatedDataRequests(identifierValue, dataTypes, reportingPeriod),
        )
    }

    override fun postSingleDataRequest(singleDataRequest: SingleDataRequest): ResponseEntity<List<StoredDataRequest>> {
        return ResponseEntity.ok(singleDataRequestManager.processSingleDataRequest(singleDataRequest))
    }

    override fun getDataRequestById(dataRequestId: UUID): ResponseEntity<StoredDataRequest> {
        return ResponseEntity.ok(dataRequestQueryManager.getDataRequestById(dataRequestId.toString()))
    }

    override fun getDataRequests(
        dataType: DataTypeEnum?,
        userId: String?,
        requestStatus: RequestStatus?,
        reportingPeriod: String?,
        dataRequestCompanyIdentifierValue: String?,
    ): ResponseEntity<List<StoredDataRequest>> {
        return ResponseEntity.ok(
            dataRequestQueryManager.getDataRequests(
                dataType,
                userId,
                requestStatus,
                reportingPeriod,
                dataRequestCompanyIdentifierValue,
            ),
        )
    }

    override fun patchDataRequest(
        dataRequestId: UUID,
        requestStatus: RequestStatus,
    ): ResponseEntity<StoredDataRequest> {
        return ResponseEntity.ok(dataRequestAlterationManager.patchDataRequest(dataRequestId.toString(), requestStatus))
    }
}
