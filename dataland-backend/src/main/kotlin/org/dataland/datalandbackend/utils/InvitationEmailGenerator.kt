package org.dataland.datalandbackend.utils

import com.mailjet.client.transactional.SendContact
import org.dataland.datalandbackend.model.email.Email
import org.dataland.datalandbackend.model.email.EmailAttachment
import org.dataland.datalandbackend.model.email.EmailContent

class InvitationEmailGenerator {
    companion object {
        private val sender = SendContact("info@dataland.com", "Dataland")
        private val receivers = getEmails("INVITATION_REQUEST_RECEIVERS")
        private val cc = getEmails("INVITATION_REQUEST_CC")

        private fun getEmails(envName: String): List<SendContact> {
            return System.getenv(envName)?.split(";")?.map { SendContact(it) }
                ?: listOf()
        }

        fun generate(attachments: List<EmailAttachment>, requesterName: String?): Email {
            val message = "User " + (requesterName ?: "anonymous") + " requested an invitation.\nPlease review."
            val content = EmailContent(
                "Dataland Invitation Request",
                message,
                message,
                attachments
            )
            return Email(sender, receivers, cc, content)
        }
    }
}