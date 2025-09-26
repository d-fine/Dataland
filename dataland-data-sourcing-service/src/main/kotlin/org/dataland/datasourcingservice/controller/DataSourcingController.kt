package org.dataland.datasourcingservice.controller

import org.dataland.datasourcingservice.api.DataSourcingApi
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.datasourcing.ReducedDataSourcing
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

/**
 *
 */
@RestController
class DataSourcingController(
    @Autowired private val dataSourcingManager: DataSourcingManager,
) : DataSourcingApi {
    override fun getDataSourcingById(id: String): ResponseEntity<StoredDataSourcing> =
        ResponseEntity.ok(dataSourcingManager.getStoredDataSourcing(UUID.fromString(id)))

    override fun getDataSourcingForCompanyId(providerCompanyId: String): ResponseEntity<List<ReducedDataSourcing>> =
        ResponseEntity.ok(dataSourcingManager.getStoredDataSourcingForCompanyId(UUID.fromString(providerCompanyId)))

    override fun getDataSourcingByDimensions(
        companyId: String,
        dataType: String,
        reportingPeriod: String,
    ): ResponseEntity<StoredDataSourcing> =
        ResponseEntity.ok(
            dataSourcingManager.getStoredDataSourcing(
                UUID.fromString(companyId),
                reportingPeriod,
                dataType,
            ),
        )

    override fun getDataSourcingHistoryById(id: String): ResponseEntity<List<DataSourcingWithoutReferences>> =
        ResponseEntity.ok(dataSourcingManager.retrieveDataSourcingHistory(id))

    override fun patchDataSourcingState(
        id: String,
        state: DataSourcingState,
    ): ResponseEntity<ReducedDataSourcing> = ResponseEntity.ok(dataSourcingManager.patchDataSourcingState(UUID.fromString(id), state))

    override fun patchDocumentCollectorAndDataExtractor(
        id: String,
        documentCollector: String?,
        dataExtractor: String?,
        adminComment: String?,
    ): ResponseEntity<StoredDataSourcing> =
        ResponseEntity.ok(
            dataSourcingManager.patchDocumentCollectorAndDataExtractor(
                UUID.fromString(id),
                documentCollector,
                dataExtractor,
                adminComment,
            ),
        )

    override fun patchDataSourcingDocuments(
        id: String,
        documentIds: Set<String>,
        appendDocuments: Boolean,
    ): ResponseEntity<ReducedDataSourcing> =
        ResponseEntity.ok(
            dataSourcingManager.patchDataSourcingDocument(
                UUID.fromString(id),
                documentIds,
                appendDocuments,
            ),
        )

    override fun patchDateOfNextDocumentSourcingAttempt(
        id: String,
        dateOfNextDocumentSourcingAttempt: LocalDate,
    ): ResponseEntity<ReducedDataSourcing> =
        ResponseEntity
            .ok(dataSourcingManager.patchDateOfNextDocumentSourcingAttempt(UUID.fromString(id), dateOfNextDocumentSourcingAttempt))
}
