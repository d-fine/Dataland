package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataAPI
import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.DataSet
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.dataland.skyminderClient.interfaces.DataConnectorInterface
import org.dataland.skyminderClient.model.ContactInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the API for data exchange
 * @param dataStore implementation of the DataStoreInterface that defines how uploaded data is to be stored
 * @param dataConnector implementation of the DataConnectorInterface that defines how to connect to the data
 * source (e.g. skyminder)
 */

@RestController
class DataController(
    @Autowired @Qualifier("DefaultStore") var dataStore: DataStoreInterface,
    @Autowired var dataConnector: DataConnectorInterface
) : DataAPI {

    override fun getData(): ResponseEntity<List<DataSetMetaInformation>> {
        return ResponseEntity.ok(this.dataStore.listDataSets())
    }

    override fun postData(dataSet: DataSet): ResponseEntity<DataSetMetaInformation> {
        return ResponseEntity.ok(this.dataStore.addDataSet(dataSet))
    }

    override fun getDataSet(id: String): ResponseEntity<DataSet> {
        return ResponseEntity.ok(this.dataStore.getDataSet(id))
    }

    override fun getDataSkyminderRequest(countryCode: String, companyName: String):
        ResponseEntity<List<ContactInformation>> {
        return ResponseEntity.ok(
            this.dataConnector.getContactInformation(countryCode = countryCode, name = companyName)
        )
    }
}
