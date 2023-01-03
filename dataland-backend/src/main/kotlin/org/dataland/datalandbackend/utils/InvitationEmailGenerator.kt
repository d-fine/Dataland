package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.model.email.Email
import org.dataland.datalandbackend.model.email.EmailAttachment
import org.dataland.datalandbackend.model.email.EmailContact
import org.dataland.datalandbackend.model.email.EmailContent
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * A utility object which contains functions to generate invitation emails
 */
class InvitationEmailGenerator {
    private val sender = EmailContact("info@dataland.com", "Dataland")
    private val receivers = getEmailsFromEnv("INVITATION_REQUEST_RECEIVERS")
    private val cc = getEmailsFromEnv("INVITATION_REQUEST_CC")

    private fun isEmailAddressFormatValid(emailAddress: String) {
        val regexForValidEmail = Regex("^[a-zA-Z0-9_.!-]+@[a-zA-Z0-9-]+.[a-z]{2,3}\$")
        if (!regexForValidEmail.matches(emailAddress)) {
            throw InternalServerErrorApiException(
                "The email addresses provided by the environment have a wrong format."
            )
        }
    }

    private fun getEmailsFromEnv(envName: String): List<EmailContact> {
        return System.getenv(envName)!!.split(";").map {
                emailAddress ->
            isEmailAddressFormatValid(emailAddress)
            EmailContact(emailAddress)
        }
    }

    private fun buildEmailAttachment(fileToAttach: MultipartFile): EmailAttachment {
        val fileName = UUID.randomUUID().toString()
        return EmailAttachment(
            "$fileName.xlsx",
            fileToAttach.bytes,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    }

    private fun getUsernameFromSecurityContext(): String {
        val jwt = SecurityContextHolder.getContext().authentication.principal as Jwt
        return jwt.getClaimAsString("preferred_username")
    }

    private fun getUserIdFromSecurityContext(): String { // TODO duplicate method => centralize somewhere
        return SecurityContextHolder.getContext().authentication.name
    }

    private fun buildUserInfo(isSubmitterNameHidden: Boolean): String {
        val userName = getUsernameFromSecurityContext()
        val userId = getUserIdFromSecurityContext()
        return when (isSubmitterNameHidden) {
            true -> "Anonymous user"
            else -> "User $userName (Keycloak id: $userId)"
        }
    }

    /**
     * Function that generates the email to be sent
     */
    fun generate(fileToAttach: MultipartFile, isSubmitterNameHidden: Boolean): Email {
        val attachment = buildEmailAttachment(fileToAttach)
        val submitterName = buildUserInfo(isSubmitterNameHidden)
        val message = "$submitterName requested an invitation.\nPlease review."
        val content = EmailContent(
            "Dataland Invitation Request",
            message,
            message,
            attachment
        )
        return Email(sender, receivers, cc, content)
    }
}
