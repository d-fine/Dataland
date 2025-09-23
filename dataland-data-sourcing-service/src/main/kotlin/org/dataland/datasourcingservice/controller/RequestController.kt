package org.dataland.datasourcingservice.controller

import org.dataland.datasourcingservice.api.RequestApi
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.model.request.SingleRequestResponse
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.dataland.datasourcingservice.services.SingleRequestManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the requests endpoint
 */
@RestController
class RequestController(
    @Autowired private val singleRequestManager: SingleRequestManager,
) : RequestApi {
    override fun createRequest(
        singleRequest: SingleRequest,
        userId: String?,
    ): ResponseEntity<SingleRequestResponse> =
        ResponseEntity.ok(
            singleRequestManager.createRequest(singleRequest, UUID.fromString(userId)),
        )

    override fun getRequest(dataRequestId: String): ResponseEntity<StoredRequest> =
        ResponseEntity.ok(singleRequestManager.getRequest(UUID.fromString(dataRequestId)))

    override fun patchRequestState(
        dataRequestId: String,
        requestState: RequestState,
    ): ResponseEntity<StoredRequest> =
        ResponseEntity.ok(
            singleRequestManager.patchRequestState(UUID.fromString(dataRequestId), requestState),
        )

    override fun getDataSourcingHistoryById(id: String): ResponseEntity<List<StoredRequest>> =
        ResponseEntity
            .ok(singleRequestManager.retrieveRequestHistory(id))
}
