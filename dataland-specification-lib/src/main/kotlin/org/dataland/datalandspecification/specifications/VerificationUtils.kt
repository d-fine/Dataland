package org.dataland.datalandspecification.specifications

object VerificationUtils {
    private val validIdRegex = Regex("[a-zA-Z0-9_\\-]{1,255}")

    /**
     * Asserts that the given id is valid.
     */
    fun assertValidId(id: String) {
        if (!validIdRegex.matches(id)) {
            throw IllegalArgumentException(
                "Invalid id: $id. Id must have a length between 1 and 255 characters " +
                    "and only contain letters, numbers, underscores,and hyphens.",
            )
        }
    }
}
