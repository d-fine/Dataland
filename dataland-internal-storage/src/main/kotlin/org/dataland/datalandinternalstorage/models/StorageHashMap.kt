package org.dataland.datalandinternalstorage.models

import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component


/**
 * Implementation of a temporary storage business data to be inserted into the internal storage via Dataland
*/
@Component
class StorageHashMap() {
    var map = HashMap<String, String>()
}
