package org.dataland.datasourcingservice.controller

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datasourcingservice.api.DataSourcingApi
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 *
 */
@RestController
class DataSourcingController : DataSourcingApi {
    override fun getDataSourcingById(id: String): ResponseEntity<StoredDataSourcing> =
        throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")

    override fun getDataSourcingByDimensions(
        companyId: String,
        dataType: String,
        reportingPeriod: String,
    ): ResponseEntity<StoredDataSourcing> = throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")

    override fun getDataSourcingHistoryById(id: String): ResponseEntity<List<StoredDataSourcing>> =
        throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")

    override fun patchDataSourcingState(
        id: String,
        state: DataSourcingState,
    ): ResponseEntity<StoredDataSourcing> = throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")

    override fun patchDataSourcingDocuments(
        id: String,
        appendDocuments: Boolean,
        documentIds: Set<String>,
    ): ResponseEntity<StoredDataSourcing> = throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")

    override fun patchDateDocumentSourcingAttempt(
        id: String,
        date: LocalDate,
    ): ResponseEntity<StoredDataSourcing> = throw ResourceNotFoundApiException("Not yet implemented", "Not yet implemented")
}
