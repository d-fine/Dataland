package org.dataland.datasourcingservice.controller

import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datasourcingservice.api.DataSourcingApi
import org.dataland.datasourcingservice.model.datasourcing.AdminDataSourcingPatch
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingPriorityByDataDimensions
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.datasourcing.ReducedDataSourcing
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.DataSourcingQueryManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 *
 */
@RestController
class DataSourcingController
    @Autowired
    constructor(
        private val dataSourcingManager: DataSourcingManager,
        private val dataSourcingQueryManager: DataSourcingQueryManager,
    ) : DataSourcingApi {
        override fun getDataSourcingById(dataSourcingId: String): ResponseEntity<StoredDataSourcing> =
            ResponseEntity
                .ok(
                    dataSourcingManager
                        .getStoredDataSourcing(
                            ValidationUtils.convertToUUID(
                                dataSourcingId,
                            ),
                        ),
                )

        override fun getDataSourcingForCompanyId(providerCompanyId: String): ResponseEntity<List<StoredDataSourcing>> =
            ResponseEntity.ok(
                dataSourcingManager
                    .getStoredDataSourcingForCompanyId(
                        ValidationUtils.convertToUUID(
                            providerCompanyId,
                        ),
                    ),
            )

        override fun getDataSourcingHistoryById(dataSourcingId: String): ResponseEntity<List<DataSourcingWithoutReferences>> =
            ResponseEntity
                .ok(
                    dataSourcingManager
                        .retrieveDataSourcingHistory(
                            ValidationUtils.convertToUUID(
                                dataSourcingId,
                            ),
                        ),
                )

        override fun patchDataSourcingState(
            dataSourcingId: String,
            state: DataSourcingState,
        ): ResponseEntity<ReducedDataSourcing> =
            ResponseEntity
                .ok(
                    dataSourcingManager
                        .patchDataSourcingState(
                            ValidationUtils.convertToUUID(
                                dataSourcingId,
                            ),
                            state,
                        ),
                )

        override fun patchDataSourcing(
            dataSourcingId: String,
            patch: AdminDataSourcingPatch,
        ): ResponseEntity<StoredDataSourcing> =
            ResponseEntity.ok(
                dataSourcingManager.patchDataSourcing(
                    ValidationUtils.convertToUUID(dataSourcingId),
                    patch,
                ),
            )

        override fun patchDataSourcingDocuments(
            dataSourcingId: String,
            documentIds: Set<String>,
            appendDocuments: Boolean,
        ): ResponseEntity<ReducedDataSourcing> =
            ResponseEntity.ok(
                dataSourcingManager.patchDataSourcingDocument(
                    ValidationUtils.convertToUUID(dataSourcingId),
                    documentIds,
                    appendDocuments,
                ),
            )

        override fun patchDateOfNextDocumentSourcingAttempt(
            dataSourcingId: String,
            dateOfNextDocumentSourcingAttempt: LocalDate,
        ): ResponseEntity<ReducedDataSourcing> =
            ResponseEntity
                .ok(
                    dataSourcingManager
                        .patchDateOfNextDocumentSourcingAttempt(
                            ValidationUtils.convertToUUID(dataSourcingId),
                            dateOfNextDocumentSourcingAttempt,
                        ),
                )

        override fun getDataSourcingPriorities(
            dataDimensions: List<BasicDataDimensions>,
        ): ResponseEntity<List<DataSourcingPriorityByDataDimensions>> =
            ResponseEntity.ok(dataSourcingManager.getPrioritiesByDataDimensions(dataDimensions))

        override fun searchDataSourcings(
            companyId: String?,
            dataType: String?,
            reportingPeriod: String?,
            state: DataSourcingState?,
            chunkSize: Int,
            chunkIndex: Int,
        ): ResponseEntity<List<StoredDataSourcing>> =
            ResponseEntity.ok(
                dataSourcingQueryManager.searchDataSourcings(
                    companyId?.let {
                        ValidationUtils.convertToUUID(it)
                    },
                    dataType, reportingPeriod, state, chunkSize, chunkIndex,
                ),
            )
    }
