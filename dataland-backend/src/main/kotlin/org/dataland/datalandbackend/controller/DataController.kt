package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.DataAPI
import org.dataland.datalandbackend.interfaces.DataConnectorInterface
import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.ContactInformation
import org.dataland.datalandbackend.model.DataSet
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class DataController(
    @Autowired @Qualifier("DefaultStore") var dataStore: DataStoreInterface,
    @Autowired @Qualifier("DefaultConnector") var dataConnector: DataConnectorInterface
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

    override fun getDataSkyminderRequest(countryCode: String, name: String): ResponseEntity<List<ContactInformation>> {
        return ResponseEntity.ok(this.dataConnector.getContactInformation(countryCode = countryCode, name = name))
    }
}
