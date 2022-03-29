package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataAPI
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.DataManagerInputToGetData
import org.dataland.datalandbackend.model.StorableDataSet
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

    override fun postCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<T>):
        ResponseEntity<String> {
        return ResponseEntity.ok(
            dataManager.addDataSet(
                StorableDataSet(
                    companyId = companyAssociatedData.companyId,
                    dataType = dataType,
                    data = objectMapper.writeValueAsString(companyAssociatedData.data)
                )
            )
        )
    }

    override fun getCompanyAssociatedDataSet(dataId: String): ResponseEntity<CompanyAssociatedData<T>> {
        val dataset = dataManager.getData(DataManagerInputToGetData(dataId = dataId, dataType = dataType))
        return ResponseEntity.ok(
            CompanyAssociatedData(
                companyId = dataManager.searchDataMetaInfo(dataId).first().companyId,
                data = objectMapper.readValue(dataset, clazz)
            )
        )
    }
}
