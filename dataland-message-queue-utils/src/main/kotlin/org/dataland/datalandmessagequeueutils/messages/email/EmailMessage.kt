package org.dataland.datalandmessagequeueutils.messages.email

/**
 * A message that is sent to the email-service such that the email-service sends an email.
 * The [EmailMessage] class contains all information such that the email-service can build and send the email.
 * The [typedEmailContent] specifies the type of the email as well as the variables used to process the associated
 * email template. Every subtype of [TypedEmailContent] has exactly one html and text template associated.
 * The [EmailMessage] class also contains the [receiver], [cc] and [bcc] of the email.
 */
data class EmailMessage(
    val typedEmailContent: TypedEmailContent,
    val receiver: List<EmailRecipient>,
    val cc: List<EmailRecipient>,
    val bcc: List<EmailRecipient>,
)
