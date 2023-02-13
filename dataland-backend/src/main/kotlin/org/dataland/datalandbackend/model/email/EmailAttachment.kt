package org.dataland.datalandbackend.model.email

import com.mailjet.client.transactional.Attachment
import com.mailjet.client.transactional.TransactionalEmail
import java.io.ByteArrayInputStream

/**
 * A class for representing an email attachment
 */
data class EmailAttachment(
    val filename: String,
    val content: ByteArray,
    val contentType: String,
)

/**
 * Uses a list of EmailAttachment objects for the build of a TransactionalEmail
 */
fun TransactionalEmail.TransactionalEmailBuilder.attachments(attachments: Collection<EmailAttachment>):
    TransactionalEmail.TransactionalEmailBuilder {
    attachments.forEach {
        this.attachment(
            Attachment.fromInputStream(
                ByteArrayInputStream(it.content),
                it.filename,
                it.contentType,
            ),
        )
    }
    return this
}
