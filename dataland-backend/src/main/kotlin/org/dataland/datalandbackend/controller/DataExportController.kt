package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataExportApi
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackend.services.DataExportStore
import org.dataland.datalandbackendutils.model.ExportFileType
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Abstract implementation of the controller for data exchange of an abstract type T
 * @param dataExportStore service for storing data export jobs
 */
@RestController
open class DataExportController(
    private val dataExportStore: DataExportStore,
) : DataExportApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getExportJobState(exportJobId: String): ResponseEntity<ExportJobProgressState> {
        val exportJobProgressState = dataExportStore.getExportJobState(UUID.fromString(exportJobId))
        return ResponseEntity.ok(exportJobProgressState)
    }

    override fun exportCompanyAssociatedDataById(exportJobId: String): ResponseEntity<InputStreamResource> {
        val exportJob = dataExportStore.getExportJob(UUID.fromString(exportJobId))
        logger.info("Download of export job $exportJobId requested.")

        return ResponseEntity
            .ok()
            .headers(buildHttpHeadersForExport(exportJob.fileType, exportJob.frameworkName))
            .body(exportJob.fileToExport)
    }

    /**
     * Builds HTTP headers for exporting data, setting the appropriate content type and
     * content disposition for file download.
     * @param exportFileType type of export selected by user
     */

    private fun buildHttpHeadersForExport(
        exportFileType: ExportFileType,
        frameworkName: String,
    ): HttpHeaders {
        val headers = HttpHeaders()
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"))
        headers.contentType = exportFileType.mediaType
        headers.contentDisposition =
            ContentDisposition
                .attachment()
                .filename("data-export-$frameworkName-$timestamp.${exportFileType.fileExtension}")
                .build()
        return headers
    }
}
