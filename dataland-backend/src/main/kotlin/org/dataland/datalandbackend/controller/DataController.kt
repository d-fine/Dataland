package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataAPI
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.CompanyAssociatedDataSet
import org.springframework.http.ResponseEntity

/**
 * Implementation of the API for data exchange
 * @param dataManager implementation of the DataManagerInterface that defines how
 * Dataland handles data */

abstract class DataController<T>(
    var dataManager: DataManagerInterface,
    var objectMapper: ObjectMapper,
    val clazz: Class<T>
) : DataAPI<T> {
    private val dataType = clazz.toString().substringAfterLast(".")

    override fun postData(companyAssociatedDataSet: CompanyAssociatedDataSet<T>): ResponseEntity<String> {
        return ResponseEntity.ok(
            this.dataManager.addDataSet(
                StorableDataSet(
                    companyId = companyAssociatedDataSet.companyId,
                    dataType = dataType,
                    data = objectMapper.writeValueAsString(companyAssociatedDataSet.dataSet)
                )
            )
        )
    }

    override fun getCompanyAssociatedDataSet(dataId: String): ResponseEntity<CompanyAssociatedDataSet<T>> {
        val dataset = this.dataManager.getStorableDataSet(DataIdentifier(dataId = dataId, dataType = dataType))
        return ResponseEntity.ok(
            objectMapper.readValue(
                this.dataManager
                    .getDataSet(DataIdentifier(dataId = dataId, dataType = dataType)),
                clazz
            CompanyAssociatedDataSet(
                objectMapper.readValue(dataset.data, clazz), dataset.companyId
            )
        )
    }
}
