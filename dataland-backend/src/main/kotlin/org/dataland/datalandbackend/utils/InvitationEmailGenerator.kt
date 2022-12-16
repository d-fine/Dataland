package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.model.email.Email
import org.dataland.datalandbackend.model.email.EmailAttachment
import org.dataland.datalandbackend.model.email.EmailContact
import org.dataland.datalandbackend.model.email.EmailContent

/**
 * A class to generate invitation emails
 */
class InvitationEmailGenerator {
    companion object {
        private val sender = EmailContact("info@dataland.com", "Dataland")
        private val receivers = getEmails("INVITATION_REQUEST_RECEIVERS")
        private val cc = getEmails("INVITATION_REQUEST_CC")

        private fun getEmails(envName: String): List<EmailContact> {
            return (System.getenv(envName)?.split(";")?.filter { it.isNotBlank() }?.map { EmailContact(it) })
                ?: listOf()
        }
        /**
         * Function that generates the email to be send
         */
        fun generate(attachment: EmailAttachment, requesterName: String?): Email {
            val message = (requesterName ?: "An anonymous user") + " requested an invitation.\nPlease review."
            val content = EmailContent(
                "Dataland Invitation Request",
                message,
                message,
                attachment
            )
            return Email(sender, receivers, cc, content)
        }
    }
}
