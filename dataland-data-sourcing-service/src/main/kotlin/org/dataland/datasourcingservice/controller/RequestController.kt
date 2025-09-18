package org.dataland.datasourcingservice.controller

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datasourcingservice.api.RequestApi
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.DataRequest
import org.dataland.datasourcingservice.model.request.StoredDataRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the requests endpoint
 */
@RestController
class RequestController : RequestApi {
    override fun createRequest(request: DataRequest): ResponseEntity<StoredDataRequest> =
        throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")

    override fun getRequest(dataRequestId: String): ResponseEntity<StoredDataRequest> =
        throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")

    override fun patchDataRequestState(
        dataRequestId: String,
        requestState: RequestState,
    ): ResponseEntity<StoredDataRequest> = throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")
}
