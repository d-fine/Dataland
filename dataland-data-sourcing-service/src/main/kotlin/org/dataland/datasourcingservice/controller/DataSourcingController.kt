package org.dataland.datasourcingservice.controller

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datasourcingservice.api.DataSourcingApi
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
                            ValidationUtils.convertToUUIDOrThrowResourceNotFoundApiException(
                                dataSourcingId,
                            ),
                        ),
                )

        override fun getDataSourcingForCompanyId(providerCompanyId: String): ResponseEntity<List<ReducedDataSourcing>> =
            ResponseEntity.ok(
                dataSourcingManager
                    .getStoredDataSourcingForCompanyId(
                        ValidationUtils.convertToUUIDOrThrowResourceNotFoundApiException(
                            providerCompanyId,
                        ),
                    ),
            )

        override fun getDataSourcingHistoryById(dataSourcingId: String): ResponseEntity<List<DataSourcingWithoutReferences>> =
            ResponseEntity
                .ok(
                    dataSourcingManager
                        .retrieveDataSourcingHistory(
                            ValidationUtils.convertToUUIDOrThrowResourceNotFoundApiException(
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
                            ValidationUtils.convertToUUIDOrThrowResourceNotFoundApiException(
                                dataSourcingId,
                            ),
                            state,
                        ),
                )

        override fun patchProviderAndAdminComment(
            dataSourcingId: String,
            documentCollector: String?,
            dataExtractor: String?,
            adminComment: String?,
        ): ResponseEntity<StoredDataSourcing> =
            ResponseEntity.ok(
                dataSourcingManager.patchProviderAndAdminComment(
                    ValidationUtils.convertToUUIDOrThrowResourceNotFoundApiException(dataSourcingId),
                    documentCollector?.let { ValidationUtils.convertToUUIDOrThrowResourceNotFoundApiException(it) },
                    dataExtractor?.let { ValidationUtils.convertToUUIDOrThrowResourceNotFoundApiException(it) },
                    adminComment,
                ),
            )

        override fun patchDataSourcingDocuments(
            dataSourcingId: String,
            documentIds: Set<String>,
            appendDocuments: Boolean,
        ): ResponseEntity<ReducedDataSourcing> =
            ResponseEntity.ok(
                dataSourcingManager.patchDataSourcingDocument(
                    ValidationUtils.convertToUUIDOrThrowResourceNotFoundApiException(dataSourcingId),
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
                            ValidationUtils.convertToUUIDOrThrowResourceNotFoundApiException(dataSourcingId),
                            dateOfNextDocumentSourcingAttempt,
                        ),
                )

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
                        ValidationUtils.convertToUUIDOrThrowResourceNotFoundApiException(it)
                    },
                    dataType, reportingPeriod, state, chunkSize, chunkIndex,
                ),
            )
    }
