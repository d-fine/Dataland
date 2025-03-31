package org.dataland.datalandemailservice.utils

import org.dataland.datalandbackendutils.utils.isEmailAddress
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient

/**
 * Utility object for converting to and from joined strings of email addresses.
 */
object EmailStringConverter {
    /**
     * Converts the given (semicolon-separated) joined string of email addresses to the corresponding
     * list of EmailContact objects.
     */
    fun convertEmailsJoinedStringToListOfEmailContacts(emailsJoinedString: String): List<EmailContact> =
        emailsJoinedString.split(";").map { emailAddress ->
            if (emailAddress.isEmailAddress()) {
                EmailContact.create(emailAddress)
            } else {
                throw IllegalArgumentException(
                    "One email address provided by the Spring properties has a wrong format. " +
                        "The following email address was parsed from that prop and caused this error: $emailAddress. " +
                        "This email address is ignored. The service shuts down.",
                )
            }
        }

    /**
     * Converts the given (semicolon-separated) joined string of email addresses to the corresponding
     * list of EmailAddress objects.
     */
    fun convertEmailsJoinedStringToListOfEmailAddresses(emailsJoinedString: String) =
        emailsJoinedString
            .split(";")
            .map { it.trim() }
            .filter { it != "" }
            .map { EmailRecipient.EmailAddress(it) }

    /**
     * Converts a list of EmailContact objects to a joined string of email addresses separated by the two-character
     * sequence "; ". Used for building log messages.
     * Attention: This is NOT the inverse method of convertEmailsJoinedStringToListOfEmailContacts.
     */
    fun convertListOfEmailContactsToJoinedStringForLogMessage(emailContacts: List<EmailContact>): String =
        emailContacts.joinToString("; ") { emailContact ->
            emailContact.emailAddress
        }
}
