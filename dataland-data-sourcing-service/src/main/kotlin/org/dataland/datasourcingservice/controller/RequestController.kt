package org.dataland.datasourcingservice.controller

import org.dataland.datasourcingservice.api.RequestApi
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.BulkDataRequest
import org.dataland.datasourcingservice.model.request.BulkDataRequestResponse
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.model.request.SingleRequestResponse
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.dataland.datasourcingservice.services.BulkRequestManager
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.dataland.datasourcingservice.services.SingleRequestManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the requests endpoint
 */
@RestController
class RequestController
    @Autowired
    constructor(
        private val singleRequestManager: SingleRequestManager,
        private val bulkDataRequestManager: BulkRequestManager,
        private val requestQueryManager: RequestQueryManager,
    ) : RequestApi {
        override fun postBulkDataRequest(bulkDataRequest: BulkDataRequest): ResponseEntity<BulkDataRequestResponse> =
            ResponseEntity.ok(
                bulkDataRequestManager.processBulkDataRequest(bulkDataRequest),
            )

        override fun createRequest(
            singleRequest: SingleRequest,
            userId: String?,
        ): ResponseEntity<SingleRequestResponse> =
            ResponseEntity.ok(
                singleRequestManager.createRequest(singleRequest, userId?.let { UUID.fromString(it) }),
            )

        override fun getRequest(dataRequestId: String): ResponseEntity<StoredRequest> =
            ResponseEntity.ok(singleRequestManager.getRequest(UUID.fromString(dataRequestId)))

        override fun patchRequestState(
            dataRequestId: String,
            requestState: RequestState,
            adminComment: String?,
        ): ResponseEntity<StoredRequest> =
            ResponseEntity.ok(
                singleRequestManager.patchRequestState(UUID.fromString(dataRequestId), requestState, adminComment),
            )

        override fun patchRequestPriority(
            dataRequestId: String,
            requestPriority: RequestPriority,
            adminComment: String?,
        ): ResponseEntity<StoredRequest> =
            ResponseEntity.ok(
                singleRequestManager.patchRequestPriority(UUID.fromString(dataRequestId), requestPriority, adminComment),
            )

        override fun getRequestHistoryById(dataRequestId: String): ResponseEntity<List<StoredRequest>> =
            ResponseEntity
                .ok(singleRequestManager.retrieveRequestHistory(dataRequestId))

        override fun searchRequests(
            companyId: String?,
            dataType: String?,
            reportingPeriod: String?,
            state: RequestState?,
        ): ResponseEntity<List<StoredRequest>> =
            ResponseEntity.ok(
                requestQueryManager.searchRequests(
                    companyId, dataType, reportingPeriod, state,
                ),
            )
    }
