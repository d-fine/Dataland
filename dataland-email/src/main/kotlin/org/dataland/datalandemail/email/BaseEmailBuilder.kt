package org.dataland.datalandemail.email

import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/**
 * A class that manages generating emails
 */
abstract class BaseEmailBuilder(
    senderEmail: String,
    senderName: String,
) {
    protected val logger = LoggerFactory.getLogger(javaClass)

    protected val senderEmailContact =
        EmailContact(assertEmailAddressFormatAndReturnIt(senderEmail), senderName)

    protected fun assertEmailAddressFormatAndReturnIt(emailAddress: String): String {
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
}
