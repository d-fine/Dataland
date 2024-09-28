package org.dataland.datalandemailservice.email

/**
 * A class that manages generating emails
 */
abstract class PropertyStyleEmailBuilder(
    senderEmail: String,
    senderName: String,
    semicolonSeparatedReceiverEmails: String? = null,
    semicolonSeparatedCcEmails: String? = null,
) : BaseEmailBuilder(senderEmail, senderName) {
    protected val receiverEmailContacts = getEmailContactsFromStringList(semicolonSeparatedReceiverEmails)
    protected val ccEmailContacts = getEmailContactsFromStringList(semicolonSeparatedCcEmails)

    private val mailStyleHtml =
        """
            <style>
                body {
                    font-family: Arial, sans-serif;
                    color: #333;
                }
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    border-radius: 10px;
                }
                .header {
                    font-size: 24px;
                    font-weight: bold;
                    margin-bottom: 10px;
                }
                .section {
                    margin-bottom: 10px;
                }
                .bold {
                    font-weight: bold;
                }
            </style>
    """

    private val htmlHead = """
        <head>
                $mailStyleHtml
        </head>
    """

    private fun getEmailContactsFromStringList(propWithSemicolonSeperatedEmailAddresses: String?): List<EmailContact>? =
        propWithSemicolonSeperatedEmailAddresses?.split(";")?.map { emailAddressString ->
            EmailContact(assertEmailAddressFormatAndReturnIt(emailAddressString))
        }

    protected fun buildPropertyStyleEmailContent(
        subject: String,
        textTitle: String,
        htmlTitle: String,
        properties: Map<String, String?>,
    ): EmailContent =
        EmailContent(
            subject,
            buildPropertyStyleTextContent(textTitle, properties),
            buildPropertyStyleHtmlContent(htmlTitle, properties),
        )

    private fun buildPropertyStyleTextContent(
        title: String,
        properties: Map<String, String?>,
    ): String =
        StringBuilder()
            .append("$title:\n")
            .apply {
                properties.filter { it.value != null }.forEach {
                    append(it.key)
                    append(": ")
                    append(it.value)
                    append("\n")
                }
            }.toString()

    private fun buildPropertyStyleHtmlContent(
        title: String,
        properties: Map<String, String?>,
    ): String =
        StringBuilder()
            .append(
                """
        <html>
        $htmlHead
        """,
            ).computePropertyStyleHtmlBody(title, properties)
            .append(
                """
        </html>
            """,
            ).toString()
            .trimIndent()

    private fun StringBuilder.computePropertyStyleHtmlBody(
        title: String,
        properties: Map<String, String?>,
    ): StringBuilder =
        this
            .append(
                """
        <body>
        <div class="container">
        """,
            ).append(
                """
        <div class="header">$title</div>
        """,
            ).apply {
                properties.filter { it.value != null }.forEach {
                    append(
                        """
        <div class="section"> <span class="bold">${it.key}: </span> ${it.value} </div>
        """,
                    )
                }
            }.append(
                """
        </div>
        </body>
        """,
            )

    protected fun buildPropertyStyleEmail(
        subject: String,
        textTitle: String,
        htmlTitle: String,
        properties: Map<String, String?>,
    ): Email {
        requireNotNull(receiverEmailContacts)
        return Email(
            senderEmailContact,
            receiverEmailContacts,
            ccEmailContacts,
            buildPropertyStyleEmailContent(
                subject,
                textTitle,
                htmlTitle,
                properties,
            ),
        )
    }
}
