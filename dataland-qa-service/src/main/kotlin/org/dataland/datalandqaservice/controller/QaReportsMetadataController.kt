package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.QaReportsMetadataApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataAndQaReportMetadata
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportMetadataService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.*

/**
 * A REST controller for the QA reports metadata API.
 */
@RestController
class QaReportsMetadataController(
    @Autowired val qaReportMetadataService: QaReportMetadataService,
) : QaReportsMetadataApi {
    override fun getQaReportsMetadata(
        uploaderUserIds: Set<UUID>?,
        showOnlyActive: Boolean,
        qaStatus: QaStatus?,
        minUploadDate: LocalDate?,
        maxUploadDate: LocalDate?,
        companyIdentifier: String?,
    ): ResponseEntity<List<DataAndQaReportMetadata>> {
        return ResponseEntity.ok(
            qaReportMetadataService
                .searchDataAndQaReportMetadata(
                    uploaderUserIds,
                    showOnlyActive,
                    qaStatus,
                    minUploadDate,
                    maxUploadDate,
                    companyIdentifier,
                ),
        )
    }
}
