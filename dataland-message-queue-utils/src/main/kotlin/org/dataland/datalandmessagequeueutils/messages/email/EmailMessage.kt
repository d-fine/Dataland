package org.dataland.datalandmessagequeueutils.messages.email

data class EmailMessage(
    val typedEmailData: TypedEmailData,
    val receiver: List<EmailRecipient>,
    val cc: List<EmailRecipient>,
    val bcc: List<EmailRecipient>,
) 