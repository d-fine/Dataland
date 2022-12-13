package org.dataland.datalandbackend.services

import com.mailjet.client.transactional.SendContact
import org.dataland.datalandbackend.entities.RequestMetaDataEntity
import org.dataland.datalandbackend.model.ExcelFilesUploadResponse
import org.dataland.datalandbackend.model.RequestMetaData
import org.dataland.datalandbackend.model.email.EmailAttachment
import org.dataland.datalandbackend.model.email.EmailContent
import org.dataland.datalandbackend.repositories.RequestMetaDataRepository
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.UUID

/**
 * Implementation of a file manager for Dataland
 */
@Component("FileManager")
class FileManager(
    @Autowired
    private val emailSender: EmailSender,
    @Autowired private val requestMetaDataRepository: RequestMetaDataRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val temporaryFileStore = mutableMapOf<String, MultipartFile>()
    private val uploadHistory = mutableMapOf<String, List<String>>()
    private val userIdToUploadId = mutableMapOf<String, String>()

    private val defaultReceiver = SendContact("TODO@d-fine.de", "TODO") // TODO this must be changed

    private val maxFiles = 20
    private val maxBytesPerFile = 5000000

    private fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    private fun getUserId(): String {
        return SecurityContextHolder.getContext().authentication.name
    }

    private fun generateUploadId(): String {
        val timestamp = Instant.now().epochSecond.toString()
        val uniqueId = generateUUID()
        return timestamp + "_" + uniqueId
    }

    private fun securityChecks(filesToCheck: List<MultipartFile>) {
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

    private fun sendEmailWithFiles(files: List<MultipartFile>, isRequesterNameHidden: Boolean) {
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

        /* TODO: Send requester info along with excel files, if the "hidden" flag is set to "false"

            if (!isRequesterNameHidden) {
                addToMail(keycloakUserId, keycloakUserName, keycloakUserMailAddress)
            }
            else { *sendMailAsBefore* }

        * */

        emailSender.sendInfoEmail(defaultReceiver, content)
    }

    private fun removeFilesFromStorage(fileIds: List<String>) {
        fileIds.forEach { temporaryFileStore.remove(it) }
    }

    /**
     * Method to store an Excel file in a map with an associated filed ID as key.
     * @param excelFiles is the Excel file to store
     */
    fun storeExcelFiles(excelFiles: List<MultipartFile>, numberOfFiles: Int, uploadId: String) {
        securityChecks(excelFiles)

        logger.info("Storing $numberOfFiles Excel files for upload with ID $uploadId.")

        val listOfNewFileIds = mutableListOf<String>()
        excelFiles.forEachIndexed { index, singleExcelFile ->
            val returnedFileId = storeOneExcelFileAndReturnFileId(singleExcelFile, index + 1, numberOfFiles)
            listOfNewFileIds.add(returnedFileId)
        }
        uploadHistory[uploadId] = listOfNewFileIds
    }

    /**
     * Method to submit an invitation request
     *  @param excelFiles is the Excel file to store
     * @return a response model object with info about the upload process
     */
    fun submitInvitation(excelFiles: List<MultipartFile>, isRequesterNameHidden: Boolean): ExcelFilesUploadResponse {
        val userId = getUserId()
        val numberOfFiles = excelFiles.size
        val uploadId = generateUploadId()
        userIdToUploadId[userId] = uploadId
        storeExcelFiles(excelFiles, numberOfFiles, uploadId)
        val listOfFileIds = uploadHistory[uploadId]!!
        addRequestMetaData(userId, userIdToUploadId) // Emanuel: I'd like to reconsider this. Does not make sense to me.

        // sendEmailWithFiles(excelFiles, isRequesterNameHidden)
        removeFilesFromStorage(listOfFileIds)
        return ExcelFilesUploadResponse(uploadId, true, "Successfully stored $numberOfFiles Excel file/s.")
    }
    /**
     * Method to add the metadata of an invitation request
     * @param userId denotes information about user
     * @param userIdToUploadId denotes information about userId-uploadId relationship
     * @return information of the newly created entry in request metadata database
     * including the generated company ID
     */
    @Transactional
    fun addRequestMetaData(userId: String, userIdToUploadId: MutableMap<String, String>): RequestMetaData {
        val requestTimestamp = Instant.now().epochSecond.toString()
        val uploadId = userIdToUploadId[userId]!!
        val requestMetaData = RequestMetaData(
            userId,
            uploadId,
            requestTimestamp,
        )
        logger.info("Creating Request MetaData entry with ID $uploadId!!")
        createStoredRequestMetaData(requestMetaData)
        return requestMetaData
    }
    private fun createStoredRequestMetaData(
        requestMetaData: RequestMetaData
    ): RequestMetaDataEntity {

        val newRequestMetaDataEntity = RequestMetaDataEntity(
            uploadId = requestMetaData.uploadId,
            userId = requestMetaData.userId,
            timeStamp = requestMetaData.requestTimestamp,
        )
        return requestMetaDataRepository.save(newRequestMetaDataEntity)
    }

    /**
     * Method to submit an invitation request
     */
    fun resetInvitation(): RequestMetaData {
        val userId = getUserId()
        val uploadId = userIdToUploadId[userId]
        val listOfFileIds: List<String> = uploadHistory[uploadId]!!
        val excelFiles = mutableListOf<MultipartFile>()
        listOfFileIds.forEach { FileId ->
            val singleExcelFile = temporaryFileStore[FileId]!!
            excelFiles.add(singleExcelFile)
        }

        return addRequestMetaData(userId, userIdToUploadId)
    }
}
