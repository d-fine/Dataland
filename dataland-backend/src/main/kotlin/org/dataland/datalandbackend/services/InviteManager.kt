package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.InviteMetaInfoEntity
import org.dataland.datalandbackend.model.InviteResult
import org.dataland.datalandbackend.model.email.EmailAttachment
import org.dataland.datalandbackend.repositories.InviteMetaInfoRepository
import org.dataland.datalandbackend.utils.InvitationEmailGenerator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.UUID
import javax.servlet.http.HttpServletRequest

/**
 * Implementation of a file manager for Dataland
 */
@Component("FileManager")
class InviteManager(
    @Autowired private val emailSender: EmailSender,
    @Autowired private val inviteMetaInfoRepository: InviteMetaInfoRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val temporaryFileStore = mutableMapOf<String, MultipartFile>()

    private val regexForValidExcelFileName = Regex("^[A-Za-z0-9-_]+.xlsx\$")

    private val inviteResultInvalidFileName = "The name of your Excel file does not match with the expected format. " +
        "Please use alphanumeric characters, hyphens and underscores only, " +
        "and make sure that your Excel file has the .xlsx format."
    private val inviteResultEmailError =
        "Your invite failed due to an error that occurred when Dataland was trying to forward your Excel file by " +
            "sending an email to a Dataland administrator. Please try again or contact us."
    private val inviteResultSuccess = "The invite was successfully processed. " +
        "Dataland administrator will look into your uploaded Excel file and take action."

    private fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    private fun getUserIdFromSecurityContext(): String {
        return SecurityContextHolder.getContext().authentication.name
    }

    private fun getUsernameFromSecurityContext(): String {
        val jwt = SecurityContextHolder.getContext().authentication.principal as Jwt
        return jwt.getClaimAsString("preferred_username")
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
        logger.info("Removing Excel file with file ID $fileId, which was originally stored for invite ID " +
                "$associatedInviteId")
        temporaryFileStore.remove(fileId)
        logger.info("Removed Excel file from in-memory-storage.")
    }

    private fun getRequest(): HttpServletRequest {
        val attribs = RequestContextHolder.getRequestAttributes()
        if (attribs != null) {
            return (attribs as ServletRequestAttributes).request
        }
        throw IllegalArgumentException("Request must not be null!")
    }

    private fun sendEmailWithFile(file: MultipartFile, isRequesterNameHidden: Boolean, fileId: String,
                                  associatedInviteId: String): Boolean {
        val noEmail = getRequest().getHeader("DATALAND-NO-EMAIL")
        if (noEmail == "true") {
            logger.info("No emails will be sent by this invitation request.")
            return false
        }
        logger.info("Sending E-Mails with invite Excel file ID $fileId for invite with ID $associatedInviteId.")
        val attachment = EmailAttachment(
            "${generateUUID()}.xlsx",
            file.bytes,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
        val requesterName = when (isRequesterNameHidden) {
            true -> null
            else -> "User ${getUsernameFromSecurityContext()} (Keycloak id: ${getUserIdFromSecurityContext()})"
        }
        val email = InvitationEmailGenerator.generate(attachment, requesterName)
        val isEmailSent = emailSender.sendEmail(email)
        return if (isEmailSent) {
            logger.info("Emails were sent.")
            true
        } else {
            logger.info("Emails could not be sent.")
            false
        }
    }

    /**
     * Method to submit an invite
     * @param excelFile is the Excel file to submit, which contains the invite info
     * @param isRequesterNameHidden decides if info about the requester of the invite shall be included
     * @return a response model object with info about the invite process
     */
    fun submitInvitation(excelFile: MultipartFile, isRequesterNameHidden: Boolean): InviteMetaInfoEntity {
        val inviteId = generateUUID()
        val fileId = storeOneExcelFileAndReturnFileId(excelFile, inviteId)
        val userId = getUserIdFromSecurityContext()
        if (!checkFilename(excelFile)) {
            removeFileFromStorage(fileId, inviteId)
            return storeMetaInfoAboutInviteInDatabase(userId, inviteId, fileId, InviteResult(false,
                inviteResultInvalidFileName))
        }
        if (!sendEmailWithFile(excelFile, isRequesterNameHidden, fileId, inviteId)) {
            removeFileFromStorage(fileId, inviteId)
            return storeMetaInfoAboutInviteInDatabase(userId, inviteId, fileId, InviteResult(false,
                inviteResultEmailError))
        }
        removeFileFromStorage(fileId, inviteId)
        return storeMetaInfoAboutInviteInDatabase(userId, inviteId, fileId, InviteResult(true,
            inviteResultSuccess)) //TODO to many return values
    }

    private fun storeMetaInfoAboutInviteInDatabase(
        userId: String,
        inviteId: String,
        fileId: String,
        inviteResult: InviteResult
    ): InviteMetaInfoEntity {
        val timestampInEpochSeconds = Instant.now().epochSecond.toString()
        val newInviteMetaInfoEntity = InviteMetaInfoEntity(inviteId, userId, fileId, timestampInEpochSeconds,
            inviteResult.isInviteSuccessful, inviteResult.inviteResultMessage)
        return inviteMetaInfoRepository.save(newInviteMetaInfoEntity)
    }
}
