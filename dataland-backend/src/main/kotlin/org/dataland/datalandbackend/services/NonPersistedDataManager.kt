package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.StorageHashMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
import java.util.*

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param companyManager service for managing company data
 * @param metaDataManager service for managing metadata
 * @param storageClient service for managing data
*/
@ComponentScan(basePackages = ["org.dataland"])
@Component("NonPersistedDataManager")
class NonPersistedDataManager(
    @Autowired var dataInformationHashMap : StorageHashMap,
    @Autowired var objectMapper: ObjectMapper

) {
    //TODO fix type missmatch, check why objectmapper is necessary
    fun selectDataSetForInternalStorage(dataId: String): String {
        return objectMapper.writeValueAsString(dataInformationHashMap.map[dataId]!!)
    }
}
