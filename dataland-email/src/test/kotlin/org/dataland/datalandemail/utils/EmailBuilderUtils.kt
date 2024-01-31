package org.dataland.datalandemail.utils

import org.dataland.datalandemail.email.Email
import org.dataland.datalandemail.email.EmailContact
import org.junit.jupiter.api.Assertions

fun assertEmailContactInformationEquals(
    expectedSenderEmail: String,
    expectedSenderName: String,
    expectedReceiverEmails: List<String>,
    expectedCcEmails: List<String>,
    email: Email,
) {
    Assertions.assertEquals(EmailContact(expectedSenderEmail, expectedSenderName), email.sender)
    Assertions.assertEquals(expectedReceiverEmails.size, email.receivers.size)
    expectedReceiverEmails.forEachIndexed { index, it ->
        Assertions.assertEquals(EmailContact(it), email.receivers[index])
    }
    Assertions.assertEquals(expectedCcEmails.size, email.cc!!.size)
    expectedCcEmails.forEachIndexed { index, it ->
        Assertions.assertEquals(EmailContact(it), email.cc!![index])
    }
}
