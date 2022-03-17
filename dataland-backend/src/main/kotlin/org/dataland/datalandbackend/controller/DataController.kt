package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataAPI
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.StoredDataSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the API for data exchange
 * @param dataManager implementation of the DataManagerInterface that defines how
 * Dataland handles data */

@RestController
abstract class DataController<T>(
    @Autowired @Qualifier("DefaultManager") var dataManager: DataManagerInterface,
    var objectMapper: ObjectMapper
) : DataAPI<T> {
    private val dataType = getClazz().toString().substringAfterLast(".")

    /**
     * Method to get the class of the abstract T
     */
    abstract fun getClazz(): Class<T>
/*
    override fun getData(): ResponseEntity<List<DataSetMetaInformation>> {
        return ResponseEntity.ok(this.dataStore.listDataSets())
    }
*/
    override fun postData(companyId: String, dataSet: T): ResponseEntity<String> {
        return ResponseEntity.ok(
            this.dataManager.addDataSet(
                StoredDataSet(
                    companyId = companyId,
                    dataType = dataType,
                    data = objectMapper.writeValueAsString(dataSet)
                )
            )
        )
    }

    override fun getDataSet(dataId: String): ResponseEntity<T> {
        return ResponseEntity.ok(
            objectMapper.readValue(
                this.dataManager
                    .getDataSet(DataIdentifier(dataId = dataId, dataType = dataType)),
                getClazz()
            )
        )
    }
}
