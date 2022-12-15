package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.InviteMetaInfoEntity
import org.dataland.datalandbackend.model.email.EmailAttachment
import org.dataland.datalandbackend.repositories.InviteMetaInfoRepository
import org.dataland.datalandbackend.utils.InvitationEmailGenerator
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.UUID
import org.springframework.security.oauth2.jwt.Jwt

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

    private val maxBytesPerFile = 5000000 // TODO nginx has also a max file size limit! configure it!
    // TODO These "magic number" could go into our applicaton properties, or be handled as Env for all Microservices

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

    private fun securityChecks(fileToCheck: MultipartFile, associatedInviteId: String) {
        logger.info("Scanning file provided via invite ID $associatedInviteId for any violations or potential risks.")
        if (fileToCheck.bytes.size > maxBytesPerFile) {
            throw InvalidInputApiException(
                "Provided file is too large.",
                "The provided file is larger than the maximum of $maxBytesPerFile."
            )
        }
            // checkFileForRisks(fileToCheck)
            // TODO we DEFINITELY need some security checks to avoid any attack vectors
            // TODO alternatively, copy individual entries to a fresh template and process that
            // e.g. filename, filetype, actual contents etc.
        logger.info("Finished scanning file.")
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
        logger.info("Removing Excel file with file ID $fileId, which was originally stored for invite ID $associatedInviteId")
        temporaryFileStore.remove(fileId)
        logger.info("Removed Excel file from in-memory-storage.")
    }

    private fun sendEmailWithFile(file: MultipartFile, isRequesterNameHidden: Boolean, fileId: String, associatedInviteId: String) {
        logger.info("Sending E-Mails with invite Excel file ID $fileId for invite with ID $associatedInviteId.")
        val attachment = EmailAttachment(
                "${generateUUID()}.xlsx",
                file.bytes,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
        val requesterName = when (isRequesterNameHidden){
            true -> null
            else -> "User ${getUsernameFromSecurityContext()} (Keycloak id: ${getUserIdFromSecurityContext()})"
        }
        val email = InvitationEmailGenerator.generate(attachment, requesterName)
        emailSender.sendEmail(email)
        logger.info("Emails were sent.")
    }

    /**
     * Method to submit an invite
     * @param excelFile is the Excel file to submit, which contains the invite info
     * @param isRequesterNameHidden decides if info about the requester of the invite shall be included
     * @return a response model object with info about the invite process
     */
    fun submitInvitation(excelFile: MultipartFile, isRequesterNameHidden: Boolean): InviteMetaInfoEntity {
        var isInviteSuccessful:Boolean
        var inviteMessage:String
        val inviteId = generateUUID()
        securityChecks(excelFile, inviteId) // if this fails, then set inviteSuccessful to "false" TODO
        val fileId = storeOneExcelFileAndReturnFileId(excelFile, inviteId)
        val userId = getUserIdFromSecurityContext()
        sendEmailWithFile(excelFile, isRequesterNameHidden, fileId, inviteId)  // if this fails, then set inviteSuccessful to "false" TODO
        removeFileFromStorage(fileId, inviteId)
        isInviteSuccessful = true
        inviteMessage = "Invite was successfully processed."
        return storeMetaInfoAboutInviteInDatabase(userId, inviteId, fileId, isInviteSuccessful, inviteMessage)
    }

    private fun storeMetaInfoAboutInviteInDatabase(
        userId:String, inviteId:String, fileId: String, inviteSuccessful: Boolean, inviteMessage:String
    ): InviteMetaInfoEntity {
        val timestampInEpochSeconds = Instant.now().epochSecond.toString()
        val newInviteMetaInfoEntity = InviteMetaInfoEntity(inviteId, userId, fileId, timestampInEpochSeconds, inviteSuccessful, inviteMessage)
        return inviteMetaInfoRepository.save(newInviteMetaInfoEntity)
    }
}
