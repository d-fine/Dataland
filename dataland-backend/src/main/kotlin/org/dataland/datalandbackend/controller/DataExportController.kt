package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataExportApi
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackendutils.model.ExportFileType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the DataExportApi; Controller used for exporting datasets to file
 * @param dataExportService service to export data
 */
@RestController
class DataExportController
    @Autowired
    constructor(
        val dataExportService: DataExportService,
        val metaDataController: MetaDataController,
        val objectMapper: ObjectMapper,
    ) : DataExportApi {
        override fun exportData(
            format: ExportFileType,
            dataId: String,
        ): ResponseEntity<String> {
            val exportFileType: ExportFileType = format

//        try {
//            exportFileType = ExportFileType.valueOf(format.lowercase())
//        } catch (ex: IllegalArgumentException) {
//            throw InvalidInputApiException(
//                summary = "File type $format not supported.",
//                message = "The file type you specified is not supported for exporting data." ,
//                cause=ex
//            )
//        }
            val metadata = objectMapper.writeValueAsString(metaDataController.getDataMetaInfo(dataId))
            val headers =
                HttpHeaders().apply {
                    contentType = exportFileType.mediaType
                    contentDisposition =
                        ContentDisposition.attachment().filename("$dataId.${exportFileType.fileExtension}").build()
                }

            val response =
                ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(exportFileType.mediaType)
                    .body(metadata)
            return response
        }
    }
