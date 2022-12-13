package org.dataland.datalandinternalstorage.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Simple implementation of a data store using a postgres database
 */
@Component
class DatabaseDataStore(
    @Autowired private var dataItemRepository: DataItemRepository
) {

    /**
     * Insterts data into a database
     * @param data a json object
     * @return id associated with the stored data
     */
    fun insertDataSet(data: String): String {
        val dataID = "${UUID.randomUUID()}:${UUID.randomUUID()}_${UUID.randomUUID()}"
        dataItemRepository.save(DataItem(dataID, data))
        return dataID
    }

    /**
     * Reads data from a database
     * @param dataId the id of the data to be retrieved
     * @return the data as json string with id dataId
     */
    fun selectDataSet(dataId: String): String {
        return dataItemRepository.findById(dataId).orElseThrow {
            InvalidInputApiException(
                "You provided an invalid data id.",
                "There is no data with id $dataId stored."
            )
        }.data
    }
}
