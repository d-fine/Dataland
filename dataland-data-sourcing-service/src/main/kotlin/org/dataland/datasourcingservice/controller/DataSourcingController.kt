package org.dataland.datasourcingservice.controller

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datasourcingservice.api.DataSourcingApi
import org.dataland.datasourcingservice.model.DataSourcingResponse
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.SortedSet

/**
 *
 */
@RestController
class DataSourcingController : DataSourcingApi {
    override fun getDataSourcingById(id: String): ResponseEntity<DataSourcingResponse> =
        throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")

    override fun getDataSourcingByDimensions(
        companyId: String,
        dataType: String,
        reportingPeriod: String,
    ): ResponseEntity<DataSourcingResponse> = throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")

    override fun getDataSourcingHistoryById(id: String): ResponseEntity<List<DataSourcingResponse>> =
        throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")

    override fun patchDataSourcingState(
        id: String,
        state: DataSourcingState,
    ): ResponseEntity<DataSourcingResponse> = throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")

    override fun patchDataSourcingDocuments(
        id: String,
        appendDocuments: Boolean,
        documentIds: Set<String>,
    ): ResponseEntity<DataSourcingResponse> = throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")

    override fun patchDateDocumentSourcingAttempt(
        id: String,
        request: SortedSet<LocalDate>,
    ): ResponseEntity<DataSourcingResponse> = throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")
}
