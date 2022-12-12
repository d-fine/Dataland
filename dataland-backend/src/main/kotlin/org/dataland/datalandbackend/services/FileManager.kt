package org.dataland.datalandbackend.services

import com.mailjet.client.transactional.SendContact
import org.dataland.datalandbackend.model.ExcelFilesUploadResponse
import org.dataland.datalandbackend.model.email.EmailAttachment
import org.dataland.datalandbackend.model.email.EmailContent
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.UUID

/**
 * Implementation of a file manager for Dataland
 */
@Component("FileManager")
class FileManager(
    @Autowired
    private val emailSender: EmailSender
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val temporaryFileStore = mutableMapOf<String, MultipartFile>()
    private val uploadHistory = mutableMapOf<String, List<String>>()

    private val defaultReceiver = SendContact("TODO@d-fine.de", "TODO") // TODO this must be changed

    private fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    private fun generateUploadId(): String {
        val timestamp = Instant.now().epochSecond.toString()
        val uniqueId = generateUUID()
        return timestamp + "_" + uniqueId
    }

    private fun securityChecks(filesToCheck: List<MultipartFile>, maxFiles: Int, maxBytesPerFile: Int) {
        val numberOfFiles = filesToCheck.size
        if (numberOfFiles > maxFiles) {
            throw InvalidInputApiException(
                "Too many files uploaded",
                "$numberOfFiles files were uploaded, but only $maxFiles are allowed."
            )
        }
        if (filesToCheck.any { it.bytes.size > maxBytesPerFile }) {
            throw InvalidInputApiException(
                "Upload file too large.",
                "An uploaded file is larger than the maximum of $maxBytesPerFile."
            )
        }
        logger.info("Scanning $numberOfFiles files for potential risks.")
        filesToCheck.forEachIndexed() { index, file ->
            // TODO we DEFINITELY need some security checks to avoid any attack vectors
            // TODO alternatively, copy individual entries to a fresh template and process that
            // e.g. filename, filetype, actual contents etc.
            logger.info("Scanned ${index + 1} of $numberOfFiles files.")
        }
    }

    private fun storeOneExcelFileAndReturnFileId(
        singleExcelFile: MultipartFile,
        positionInQueue: Int,
        totalQueueLength: Int
    ): String {
        val fileId = generateUUID()
        logger.info("Storing Excel file with file ID $fileId. (File $positionInQueue of $totalQueueLength files.)")
        temporaryFileStore[fileId] = singleExcelFile
        logger.info("Excel file with file ID $fileId was stored in-memory.")
        return fileId
    }

    private fun sendEmailWithFiles(files: List<MultipartFile>) {
        val content = EmailContent(
            "Dataland Excel Upload",
            "Someone uploaded files to Dataland.\nPlease review.",
            "Someone uploaded files to Dataland.<br>Please review.",
            files.stream().map {
                EmailAttachment(
                    "${generateUUID()}.xlsx",
                    it.bytes,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            }.toList()
        )
        emailSender.sendInfoEmail(defaultReceiver, content)
    }

    private fun removeFilesFromStorage(fileIds: List<String>) {
        fileIds.forEach { temporaryFileStore.remove(it) }
    }

    /**
     * Method to store an Excel file in a map with an associated filed ID as key.
     * @param excelFiles is the Excel file to store
     * @return a response model object with info about the upload process
     */
    fun storeExcelFiles(excelFiles: List<MultipartFile>): ExcelFilesUploadResponse {
        securityChecks(excelFiles, 20, 5000000)

        val numberOfFiles = excelFiles.size
        val uploadId = generateUploadId()
        logger.info("Storing $numberOfFiles Excel files for upload with ID $uploadId.")

        val listOfNewFileIds = mutableListOf<String>()
        excelFiles.forEachIndexed { index, singleExcelFile ->
            val returnedFileId = storeOneExcelFileAndReturnFileId(singleExcelFile, index + 1, numberOfFiles)
            listOfNewFileIds.add(returnedFileId)
        }
        uploadHistory[uploadId] = listOfNewFileIds
        sendEmailWithFiles(excelFiles)
        removeFilesFromStorage(listOfNewFileIds)

        return ExcelFilesUploadResponse(uploadId, true, "Successfully stored $numberOfFiles Excel files.")
    }

    /**
     * Method to find a specific Excel file in a map by looking for its file ID, and then returning the Excel file.
     * @param excelFileId is the identifier which is needed to identify the required Excel file
     * @return the actual Excel file
     */
    fun provideExcelFile(excelFileId: String): MultipartFile {
        logger.info("Searching for Excel file with file ID $excelFileId in in-memory storage.")
        if (temporaryFileStore.containsKey(excelFileId)) {
            return temporaryFileStore[excelFileId]!!
        }
        throw ResourceNotFoundApiException("File not found", "Dataland does not know the file ID $excelFileId")
    }
}
