package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.FileApi
import org.dataland.datalandbackend.model.ExcelFilesUploadResponse
import org.dataland.datalandbackend.model.RequestMetaData
import org.dataland.datalandbackend.services.FileManager
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
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
class FileController(
    @Autowired var fileManager: FileManager,
) : FileApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun submitInvitation(excelFiles: List<MultipartFile>?): ResponseEntity<ExcelFilesUploadResponse> {
        if (excelFiles.isNullOrEmpty()) {
            throw InvalidInputApiException(
                "Some input files must be specified.",
                "This endpoint requires files to contain at least one file."
            )
        }
        logger.info("Received a request to store ${excelFiles.size} Excel files.")
        return ResponseEntity.ok(fileManager.submitInvitation(excelFiles))
    }

    override fun resetInvitationInMemoryStorage(): ResponseEntity<RequestMetaData> {
        return ResponseEntity.ok(fileManager.resetInvitation())
    }
}
