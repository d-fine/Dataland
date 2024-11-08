package org.dataland.datalandmessagequeueutils.messages.email

data class EmailMessage(
    val typedEmailContent: TypedEmailContent,
    val receiver: List<EmailRecipient>,
    val cc: List<EmailRecipient>,
    val bcc: List<EmailRecipient>,
) 