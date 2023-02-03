package org.dataland.datalandbackend.model

import org.springframework.stereotype.Component

/**
 * Implementation of a temporary storage business data to be inserted into the internal storage via Dataland
*/
@Component
class StorageHashMap() {
    var map = HashMap<String, String>()
}
