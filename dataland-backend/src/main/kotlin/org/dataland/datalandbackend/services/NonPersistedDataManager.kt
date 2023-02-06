package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.StorageHashMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param dataInformationHashMap temporarily datastore
*/
@ComponentScan(basePackages = ["org.dataland"])
@Component("NonPersistedDataManager")
class NonPersistedDataManager(
    @Autowired var dataInformationHashMap: StorageHashMap,
    @Autowired var objectMapper: ObjectMapper

) {
    // TODO fix type missmatch, check why objectmapper is necessary
    /**
     * This method retrieves data from the temporal storage
     * @param dataId is the identifier for which all stored data entries in the temporary storage are filtered
     */
    fun selectDataSetForInternalStorage(dataId: String): String {
        return objectMapper.writeValueAsString(dataInformationHashMap.map[dataId]!!)
    }
}
