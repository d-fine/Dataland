package org.dataland.datalandbackendutils.services

import java.util.*

/**
 * Method to generate a random Data ID
 * @return generated UUID
 */
fun generateRandomUuid(): String { // TODO move this into a util class, because PrivateDataManager uses it too now
    return "${UUID.randomUUID()}"
}
