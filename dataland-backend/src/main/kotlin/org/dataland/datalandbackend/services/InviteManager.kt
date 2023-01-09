package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.InviteMetaInfoEntity
import org.dataland.datalandbackend.model.InviteResult
import org.dataland.datalandbackend.repositories.InviteMetaInfoRepository
import org.dataland.datalandbackend.utils.InvitationEmailGenerator
import org.dataland.datalandbackend.utils.KeycloakUserUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.UUID

/**
 * Implementation of a invite manager for Dataland
 */
@Component("FileManager")
class InviteManager(
    @Autowired private val emailSender: EmailSender,
    @Autowired private val inviteMetaInfoRepository: InviteMetaInfoRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val temporaryFileStore = mutableMapOf<String, MultipartFile>()

    private val regexForValidExcelFileName = Regex("\\.xlsx\$")

    private val inviteResultInvalidFileName = "The name of your Excel file does not match with the expected format. " +
        "Please make sure that your Excel file has the .xlsx format."
    private val inviteResultFileIsEmpty = "Your Excel file is empty. Please make sure to upload a valid file."
    private val inviteResultEmailError =
        "Your invite failed due to an error that occurred when Dataland was trying to forward your Excel file by " +
            "sending an email to a Dataland administrator. Please try again or contact us."
    private val inviteResultSuccess = "Your data request was submitted. You will be notified about its state via email."

    private fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    private fun checkFilename(fileToCheck: MultipartFile): Boolean {
        return regexForValidExcelFileName.matches(fileToCheck.originalFilename!!)
    }

    private fun storeOneExcelFileAndReturnFileId(
        singleExcelFile: MultipartFile,
        associatedInviteId: String
    ): String {
        val fileId = generateUUID()
        logger.info("Storing Excel file with file ID $fileId for invite ID $associatedInviteId.")
        temporaryFileStore[fileId] = singleExcelFile
        logger.info("Excel file was stored in-memory.")
        return fileId
    }

    private fun removeFileFromStorage(fileId: String, associatedInviteId: String) {
        logger.info(
            "Removing Excel file with file ID $fileId, which was originally stored for invite ID $associatedInviteId"
        )
        temporaryFileStore.remove(fileId)
        logger.info("Removed Excel file from in-memory-storage.")
    }

    private fun sendEmailWithFile(
        file: MultipartFile,
        isSubmitterNameHidden: Boolean,
        fileId: String,
        associatedInviteId: String
    ): Boolean {
        logger.info("Sending E-Mails with invite Excel file ID $fileId for invite with ID $associatedInviteId.")
        val email = InvitationEmailGenerator.generate(file, isSubmitterNameHidden)
        val isEmailSent = emailSender.sendEmail(email)
        return if (isEmailSent) {
            logger.info("Emails were sent.")
            true
        } else {
            logger.info("Emails could not be sent.")
            false
        }
    }

    private fun handleSubmission(
        fileId: String,
        inviteId: String,
        success: Boolean,
        message: String
    ): InviteMetaInfoEntity {
        val userId = KeycloakUserUtils.getUserIdFromSecurityContext()
        removeFileFromStorage(fileId, inviteId)
        return storeMetaInfoAboutInviteInDatabase(userId, inviteId, fileId, InviteResult(success, message))
    }

    /**
     * Method to submit an invite
     * @param excelFile is the Excel file to submit, which contains the invite info
     * @param isSubmitterNameHidden decides if info about the submitter of the invite shall be included
     * @return a response model object with info about the invite process
     */
    fun submitInvitation(excelFile: MultipartFile, isSubmitterNameHidden: Boolean): InviteMetaInfoEntity {
        val inviteId = generateUUID()
        val fileId = storeOneExcelFileAndReturnFileId(excelFile, inviteId)
        return if (!checkFilename(excelFile)) {
            handleSubmission(fileId, inviteId, false, inviteResultInvalidFileName)
        } else if (excelFile.isEmpty) {
            handleSubmission(fileId, inviteId, false, inviteResultFileIsEmpty)
        } else if (!sendEmailWithFile(excelFile, isSubmitterNameHidden, fileId, inviteId)) {
            handleSubmission(fileId, inviteId, false, inviteResultEmailError)
        } else handleSubmission(fileId, inviteId, true, inviteResultSuccess)
    }

    private fun storeMetaInfoAboutInviteInDatabase(
        userId: String,
        inviteId: String,
        fileId: String,
        inviteResult: InviteResult
    ): InviteMetaInfoEntity {
        val timestampInEpochSeconds = Instant.now().epochSecond.toString()
        val newInviteMetaInfoEntity = InviteMetaInfoEntity(
            inviteId, userId, fileId, timestampInEpochSeconds,
            inviteResult.isInviteSuccessful,
            inviteResult.inviteResultMessage
        )
        return inviteMetaInfoRepository.save(newInviteMetaInfoEntity)
    }
}
