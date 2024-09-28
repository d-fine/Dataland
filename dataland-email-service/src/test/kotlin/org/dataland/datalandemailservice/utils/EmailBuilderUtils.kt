package org.dataland.datalandemailservice.utils

import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailContact
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

fun assertEmailContactInformationEquals(
    expectedSender: EmailContact,
    expectedReceivers: Set<EmailContact>,
    expectedCc: Set<EmailContact>,
    email: Email,
) {
    assertEquals(expectedSender, email.sender)
    assertEquals(expectedReceivers, email.receivers.toSet())
    assertEquals(expectedCc, email.cc?.toSet() ?: emptySet<EmailContact>())
}

fun List<String>.toEmailContacts() = map { EmailContact(it) }.toSet()

data class EmailMatchingPattern(
    val expectedSender: EmailContact,
    val expectedReceiversGetter: () -> Set<EmailContact>,
    val expectedCc: Set<EmailContact>,
    val expectedSubject: String,
    val expectedToBeContainedInTextContent: Set<String>,
    val expectedToBeContainedInHtmlContent: Set<String>,
    val expectedNotToBeContainedInTextContent: Set<String> = emptySet(),
    val expectedNotToBeContainedInHtmlContent: Set<String> = emptySet(),
)

fun assertEmailMatchesPattern(
    email: Email,
    validationPattern: EmailMatchingPattern,
) {
    assertEmailContactInformationEquals(
        validationPattern.expectedSender,
        validationPattern.expectedReceiversGetter(),
        validationPattern.expectedCc,
        email,
    )
    assertEquals(validationPattern.expectedSubject, email.content.subject)
    validationPattern.expectedToBeContainedInTextContent.forEach {
        assertTrue(email.content.textContent.contains(it))
    }
    validationPattern.expectedToBeContainedInHtmlContent.forEach {
        assertTrue(email.content.htmlContent.contains(it))
    }
    validationPattern.expectedNotToBeContainedInTextContent.forEach {
        assertFalse(email.content.textContent.contains(it))
    }
    validationPattern.expectedNotToBeContainedInHtmlContent.forEach {
        assertFalse(email.content.htmlContent.contains(it))
    }
}
