package org.dataland.datasourcingservice.controller

import org.dataland.datasourcingservice.api.RequestApi
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.DataRequest
import org.dataland.datasourcingservice.model.request.StoredDataRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the requests endpoint
 */
@RestController
class RequestController : RequestApi {
    override fun createRequest(request: DataRequest): ResponseEntity<StoredDataRequest> {
        return ResponseEntity.noContent().build() // todo: not yet implemented
    }

    override fun getRequest(dataRequestId: UUID): ResponseEntity<StoredDataRequest> {
        return ResponseEntity.noContent().build() // todo: not yet implemented
    }

    override fun patchDataRequestState(
        dataRequestId: UUID,
        requestState: RequestState,
    ): ResponseEntity<StoredDataRequest> {
        return ResponseEntity.noContent().build() // todo: not yet implemented
    }
}
