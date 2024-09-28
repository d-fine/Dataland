package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

import java.util.UUID

/**
 * A class to help with id generation
 */
object IdUtils {
    /**
     * Generates a UUID-string
     * @returns the generated UUID as a string
     */
    fun generateUUID(): String = UUID.randomUUID().toString()
}
