package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.InviteMetaInfoEntity
import org.dataland.datalandbackend.model.InviteResult
import org.dataland.datalandbackend.repositories.InviteMetaInfoRepository
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.InvitationEmailGenerator
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

/**
 * Implementation of a invite manager for Dataland
 */
@Component("FileManager")
class InviteManager(
    @Autowired private val emailSender: EmailSender,
    @Autowired private val inviteMetaInfoRepository: InviteMetaInfoRepository,
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

    private fun checkFilename(fileToCheck: MultipartFile): Boolean {
        return regexForValidExcelFileName.containsMatchIn(fileToCheck.originalFilename!!)
    }

    private fun storeOneExcelFileAndReturnFileId(
        singleExcelFile: MultipartFile,
        associatedInviteId: String,
    ): String {
        val fileId = IdUtils.generateUUID()
        logger.info("Storing Excel file with file ID $fileId for invite ID $associatedInviteId.")
        temporaryFileStore[fileId] = singleExcelFile
        logger.info("Excel file was stored in-memory.")
        return fileId
    }

    private fun removeFileFromStorage(fileId: String, associatedInviteId: String) {
        logger.info(
            "Removing Excel file with file ID $fileId, which was originally stored for invite ID $associatedInviteId",
        )
        temporaryFileStore.remove(fileId)
        logger.info("Removed Excel file from in-memory-storage.")
    }

    private fun sendEmailWithFile(
        file: MultipartFile,
        isSubmitterNameHidden: Boolean,
        fileId: String,
        associatedInviteId: String,
    ): InviteResult {
        logger.info("Sending E-Mails with invite Excel file ID $fileId for invite with ID $associatedInviteId.")
        val email = InvitationEmailGenerator.generate(file, isSubmitterNameHidden)
        val isEmailSent = emailSender.sendEmail(email)
        return InviteResult(isEmailSent, if (isEmailSent) inviteResultSuccess else inviteResultEmailError)
    }

    /**
     * Method to submit an invite
     * @param excelFile is the Excel file to submit, which contains the invite info
     * @param isSubmitterNameHidden decides if info about the submitter of the invite shall be included
     * @return a response model object with info about the invite process
     */
    fun submitInvitation(excelFile: MultipartFile, isSubmitterNameHidden: Boolean): InviteMetaInfoEntity {
        if (DatalandAuthentication.fromContext() !is DatalandJwtAuthentication) {
            throw AuthenticationMethodNotSupportedException(
                "The chosen authentication method is not supported for this request. Please authenticate using a JWT.",
            )
        }

        val inviteId = IdUtils.generateUUID()
        val fileId = storeOneExcelFileAndReturnFileId(excelFile, inviteId)
        val inviteResult = if (!checkFilename(excelFile)) {
            InviteResult(false, inviteResultInvalidFileName)
        } else if (excelFile.isEmpty) {
            InviteResult(false, inviteResultFileIsEmpty)
        } else {
            sendEmailWithFile(excelFile, isSubmitterNameHidden, fileId, inviteId)
        }
        removeFileFromStorage(fileId, inviteId)
        return storeMetaInfoAboutInviteInDatabase(inviteId, fileId, inviteResult)
    }

    private fun storeMetaInfoAboutInviteInDatabase(
        inviteId: String,
        fileId: String,
        inviteResult: InviteResult,
    ): InviteMetaInfoEntity {
        val userId = DatalandAuthentication.fromContext().userId
        val timestampInEpochSeconds = Instant.now().epochSecond.toString()
        val newInviteMetaInfoEntity = InviteMetaInfoEntity(
            inviteId,
            userId,
            fileId,
            timestampInEpochSeconds,
            inviteResult.isInviteSuccessful,
            inviteResult.inviteResultMessage,
        )
        return inviteMetaInfoRepository.save(newInviteMetaInfoEntity)
    }
}
