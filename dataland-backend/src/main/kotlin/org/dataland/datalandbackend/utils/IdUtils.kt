package org.dataland.datalandbackend.utils

import java.util.UUID

/**
 * A class to help with id generation
 */
object IdUtils {
    /**
     * A method to generate a UUID
     * @return the generated UUID
     */
    fun generateUUID(): String = UUID.randomUUID().toString()
}
