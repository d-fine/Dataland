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
        userId: UUID?,
    ): ResponseEntity<SingleRequestResponse> =
        ResponseEntity.ok(
            singleRequestManager.createRequest(singleRequest, userId),
        )

    override fun getRequest(dataRequestId: UUID): ResponseEntity<StoredRequest> =
        ResponseEntity.ok(singleRequestManager.getRequest(dataRequestId))

    override fun patchRequestState(
        dataRequestId: UUID,
        requestState: RequestState,
    ): ResponseEntity<StoredRequest> =
        ResponseEntity.ok(
            singleRequestManager.patchRequestState(dataRequestId, requestState),
        )
}
