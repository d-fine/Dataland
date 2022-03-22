package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataAPI
import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.UploadableDataSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity

/**
 * Implementation of the API for data exchange
 * @param dataStore implementation of the DataStoreInterface that defines how uploaded data is to be stored
 */

abstract class DataController<T>(
    var dataStore: DataStoreInterface,
    var objectMapper: ObjectMapper,
    val clazz: Class<T>
) : DataAPI<T> {
    private val dataType = clazz.toString().substringAfterLast(".")

    override fun getData(): ResponseEntity<List<DataSetMetaInformation>> {
        return ResponseEntity.ok(this.dataStore.listDataSets())
    }

    override fun postData(uploadableDataSet: UploadableDataSet<T>): ResponseEntity<String> {
        return ResponseEntity.ok(
            this.dataStore.addDataSet(
                StorableDataSet(
                    companyId = uploadableDataSet.companyId,
                    dataType = dataType,
                    data = objectMapper.writeValueAsString(uploadableDataSet.dataSet)
                )
            )
        )
    }

    override fun getDataSet(dataId: String): ResponseEntity<T> {
        return ResponseEntity.ok(
            objectMapper.readValue(
                this.dataStore
                    .getDataSet(DataIdentifier(dataId = dataId, dataType = dataType)),
                clazz
            )
        )
    }
}
