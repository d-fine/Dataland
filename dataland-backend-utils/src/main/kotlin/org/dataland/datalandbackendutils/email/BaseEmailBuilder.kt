package org.dataland.datalandbackendutils.email

import org.slf4j.LoggerFactory
import kotlin.system.exitProcess


/**
 * A class that manages generating emails
 */
open class BaseEmailBuilder(
    senderEmail: String,
    senderName: String,
    semicolonSeparatedReceiverEmails: String,
    semicolonSeparatedCcEmails: String,
) {
    protected val logger = LoggerFactory.getLogger(javaClass)

    protected val senderEmailContact =
        EmailContact(assertEmailAddressFormatAndReturnIt(senderEmail), senderName)

    protected val receiverEmailContacts = getEmailContactsFromProp(semicolonSeparatedReceiverEmails)
    protected val ccEmailContacts = getEmailContactsFromProp(semicolonSeparatedCcEmails)

    protected val mailStyleHtml =
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

    private fun assertEmailAddressFormatAndReturnIt(emailAddress: String): String {
        val regexForValidEmail = Regex("^[a-zA-Z0-9_.!-]+@[a-zA-Z0-9-]+.[a-z]{2,3}\$")
        if (!regexForValidEmail.matches(emailAddress)) {
            logger.error(
                "The email addresses provided by the Spring properties have a wrong format. " +
                    "The following email address was parsed from that prop and caused this error: $emailAddress" +
                    "The Spring application is shutting down because sending notifications might not work as expected.",
            )
            exitProcess(1)
        }
        return emailAddress
    }

    private fun getEmailContactsFromProp(propWithSemicolonSeperatedEmailAddresses: String): List<EmailContact> {
        return propWithSemicolonSeperatedEmailAddresses.split(";").map {
                emailAddressString ->
            EmailContact(assertEmailAddressFormatAndReturnIt(emailAddressString))
        }
    }

    protected fun buildPropertyStyleEmailContent(
        subject: String,
        textTitle: String,
        htmlTitle: String,
        properties: Map<String, String>,
    ): EmailContent {
        return EmailContent(
            subject,
            buildPropertyStyleTextContent(textTitle, properties),
            buildPropertyStyleHtmlContent(htmlTitle, properties),
        )
    }

    private fun buildPropertyStyleTextContent(title: String, properties: Map<String, String>): String {
        return StringBuilder()
            .append("$title:\n")
            .apply {
                properties.forEach {
                    append(it.key)
                    append(": ")
                    append(it.value)
                    append("\n")
                }
            }
            .toString()
    }

    private fun buildPropertyStyleHtmlContent(title: String, properties: Map<String, String>): String {
        return StringBuilder()
            .append("""
        <html>
        <head>
                $mailStyleHtml
        </head>
        <body>
            <div class="container">
        """)
            .append("""
                <div class="header">$title</div>
            """)
            .apply {
                properties.forEach {
                    append("""
                <div class="section"> <span class="bold">${it.key}: </span> ${it.value} </div>
                    """)
                }
            }
            .append("""
                </div>
        </body>
        </html>
            """).toString().trimIndent()
    }
}
