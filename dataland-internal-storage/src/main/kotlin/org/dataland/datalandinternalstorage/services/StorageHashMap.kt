package org.dataland.datalandinternalstorage.services

import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component


/**
 * Implementation of a data manager for Dataland including metadata storages
*/
@Component
class StorageHashMap() {
    var map = HashMap<String, String>()
}
