package org.dataland.datalandbackendutils.utils

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException

/**
 * Checks if a string is an email address
 * @returns true if and only if the string matches email address pattern
 */
fun String.isEmailAddress() = Regex("^[a-zA-Z0-9_.!-]+@([a-zA-Z0-9-]+\\.){1,2}[a-zA-Z]{2,}\$").matches(this)

/**
 * Validates that a string is an email address and throws an exception if not
 * @throws InvalidEmailFormatApiException if the email format is violated
 */
fun String.validateIsEmailAddress() {
    if (!isEmailAddress()) {
        throw InvalidEmailFormatApiException(this)
    }
}

/**
 * An API exception which should be raised if an email format is violated
 */
class InvalidEmailFormatApiException(
    email: String,
) : InvalidInputApiException(
        "Invalid email address \"$email\"",
        "The email address \"$email\" you have provided has an invalid format.",
    )
