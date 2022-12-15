package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.RequestMetaDataEntity
import org.dataland.datalandbackend.model.RequestMetaData
import org.dataland.datalandbackend.model.email.EmailAttachment
import org.dataland.datalandbackend.repositories.RequestMetaDataRepository
import org.dataland.datalandbackend.utils.InvitationEmailGenerator
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.UUID
import org.springframework.security.oauth2.jwt.Jwt

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
    private val uploadHistory = mutableMapOf<String, String>()
    private val userIdToUploadId = mutableMapOf<String, String>()

    private val maxBytesPerFile = 5000000 // TODO nginx has also a max file size limit! configure it!
    // TODO These "magic number" could go into our applicaton properties

    private fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    private fun generateUploadId(): String {
        val timestamp = Instant.now().epochSecond.toString()
        val uniqueId = generateUUID()
        return timestamp + "_" + uniqueId
    }

    private fun getUserId(): String {
        return SecurityContextHolder.getContext().authentication.name
    }

    private fun getUsername(): String {
        val jwt = SecurityContextHolder.getContext().authentication.principal as Jwt
        return jwt.getClaimAsString("preferred_username")
    }

    private fun securityChecks(fileToCheck: MultipartFile) {
        if (fileToCheck.bytes.size > maxBytesPerFile) {
            throw InvalidInputApiException(
                "Provided file is too large.",
                "The provided file is larger than the maximum of $maxBytesPerFile."
            )
        }
        logger.info("Scanning file for potential risks.")
            // checkFileForRisks(fileToCheck)
            // TODO we DEFINITELY need some security checks to avoid any attack vectors
            // TODO alternatively, copy individual entries to a fresh template and process that
            // e.g. filename, filetype, actual contents etc.
            logger.info("Scanned file.")
        }


    private fun storeOneExcelFileAndReturnFileId(
        singleExcelFile: MultipartFile,
    ): String {
        val fileId = generateUUID()
        logger.info("Storing Excel file with file ID $fileId.")
        temporaryFileStore[fileId] = singleExcelFile
        logger.info("Excel file with file ID $fileId was stored in-memory.")
        return fileId
    }

    private fun removeFileFromStorage(fileId: String) {
        temporaryFileStore.remove(fileId)
    }

    private fun sendEmailWithFile(file: MultipartFile, isRequesterNameHidden: Boolean) {
        val attachment = EmailAttachment(
                "${generateUUID()}.xlsx",
                file.bytes,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
        val requesterName = if(isRequesterNameHidden) {
            null
        } else {
            "User ${getUsername()} (Keycloak id: ${getUserId()})"
        }
        val email = InvitationEmailGenerator.generate(attachment, requesterName)
        emailSender.sendEmail(email)
    }

    /**
     * Method to run a security check on an Excel file, then store it and document the upload in the upload history
     * @param excelFile is the Excel file to store
     */
    fun executeUploadProcess(excelFile: MultipartFile, uploadId: String) {
        securityChecks(excelFile)
        logger.info("Storing Excel file for upload with ID $uploadId.")
        val returnedFileId = storeOneExcelFileAndReturnFileId(excelFile)
        uploadHistory[uploadId] = returnedFileId
        }

    /**
     * Method to submit an invitation request
     *  @param excelFile is the Excel file to submit
     * @return a response model object with info about the upload process
     */
    fun submitInvitation(excelFile: MultipartFile, isRequesterNameHidden: Boolean): ExcelFileUploadResponse {
        val userId = getUserId()
        val uploadId = generateUploadId()
        userIdToUploadId[userId] = uploadId
        executeUploadProcess(excelFile, uploadId)
        val fileId = uploadHistory[uploadId]!!
        addRequestMetaData(userId, userIdToUploadId) // Emanuel: I'd like to reconsider this. Does not make sense to me.

        sendEmailWithFile(excelFile, isRequesterNameHidden)
        removeFileFromStorage(fileId)
        return ExcelFileUploadResponse(uploadId, true, "Successfully stored Excel file.")
    }
    /**
     * Method to add the metadata of an invitation request
     * @param userId denotes information about user
     * @param userIdToUploadId denotes information about userId-uploadId relationship
     * @return information of the newly created entry in request metadata database
     * including the generated company ID
     */
    @Transactional
    fun addRequestMetaData(userId: String, uploadId: String): RequestMetaData {
        val requestTimestamp = Instant.now().epochSecond.toString()
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
}
