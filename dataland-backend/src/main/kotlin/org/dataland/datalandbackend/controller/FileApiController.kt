package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.FileAPI
import org.dataland.datalandbackend.model.ExcelFileUploadResponse
import org.dataland.datalandbackend.services.FileManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * Controller for the file endpoints
 * @param fileManager the file manager service to handle files
 */

@RestController
class FileApiController(
    @Autowired var fileManager: FileManager
) : FileAPI {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun uploadExcelFile(excelFile: MultipartFile): ResponseEntity<ExcelFileUploadResponse> {
        logger.info("Received a request to store an Excel file.")
        return ResponseEntity.ok(fileManager.uploadExcelFile(excelFile))
    }
}
