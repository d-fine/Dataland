package org.dataland.datalandemailservice.utils

import org.dataland.datalandbackendutils.utils.isEmailAddress
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient

object EmailStringConverter {
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

    fun convertEmailsJoinedStringToListOfEmailAddresses(emailsJoinedString: String) =
        emailsJoinedString
            .split(";")
            .map { it.trim() }
            .filter { it != "" }
            .map { EmailRecipient.EmailAddress(it) }

    // Attention: The following method introduces spaces between emails in the joined string for improved
    // readability. It is therefore NOT the inverse method of convertEmailsJoinedStringToListOfEmailContacts.
    fun convertListOfEmailContactsToJoinedStringForLogMessage(emailContacts: List<EmailContact>): String =
        emailContacts.joinToString("; ") { emailContact ->
            emailContact.emailAddress
        }
}
