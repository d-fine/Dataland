package org.dataland.datalandemailservice.email

import org.dataland.datalandbackendutils.utils.isEmailAddress
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
        if (!emailAddress.isEmailAddress()) {
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
