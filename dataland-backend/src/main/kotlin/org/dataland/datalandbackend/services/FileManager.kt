package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.ExcelFilesUploadResponse
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Implementation of a file manager for Dataland
 */
@Component("FileManager")
class FileManager {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val temporaryFileStore = mutableMapOf<String, MultipartFile>()

    /**
     * Method to store an Excel file in a map with an associated filed ID as key.
     * @param excelFiles is the Excel file to store
     * @return a response model object with info about the upload process
     */
    fun uploadExcelFiles(excelFiles: List<MultipartFile>): ExcelFilesUploadResponse {
        val numberOfFiles = excelFiles.size
        logger.info("Starting upload process for $numberOfFiles Excel files.")
        excelFiles.forEachIndexed { index, singleExcelFile ->
            val fileId = UUID.randomUUID().toString()
            logger.info("Storing Excel file with file ID $fileId. (File ${index + 1} of $numberOfFiles files.)")
            temporaryFileStore[fileId] = singleExcelFile
            logger.info("Excel file with file ID $fileId was stored in-memory.")
        }
        return ExcelFilesUploadResponse(true, "Successfully stored $numberOfFiles Excel files.")
    }

    /**
     * Method to find a specific Excel file in a map by looking for its file ID, and then returning the Excel file.
     * @param excelFileId is the identifier which is needed to identify the required Excel file
     * @return the actual Excel file
     */
    fun getExcelFile(excelFileId: String): MultipartFile {
        logger.info("Searching for Excel file with file ID $excelFileId in in-memory storage.")
        if (temporaryFileStore.containsKey(excelFileId)) {
            return temporaryFileStore[excelFileId]!!
        }
        throw ResourceNotFoundApiException("File not found", "Dataland does not know the file ID $excelFileId")
    }
}
