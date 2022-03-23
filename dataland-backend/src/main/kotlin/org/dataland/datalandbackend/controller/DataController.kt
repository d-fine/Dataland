package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataAPI
import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.CompanyAssociatedDataSet
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.dataland.datalandbackend.model.StorableDataSet
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

    override fun postData(companyAssociatedDataSet: CompanyAssociatedDataSet<T>): ResponseEntity<String> {
        return ResponseEntity.ok(
            this.dataStore.addDataSet(
                StorableDataSet(
                    companyId = companyAssociatedDataSet.companyId,
                    dataType = dataType,
                    data = objectMapper.writeValueAsString(companyAssociatedDataSet.dataSet)
                )
            )
        )
    }

    override fun getCompanyAssociatedDataSet(dataId: String): ResponseEntity<CompanyAssociatedDataSet<T>> {
        val dataset = this.dataStore.getCompanyAssociatedDataSet(DataIdentifier(dataId = dataId, dataType = dataType))
        return ResponseEntity.ok(CompanyAssociatedDataSet(objectMapper.readValue(dataset[0], clazz), dataset[1]))
    }
}
