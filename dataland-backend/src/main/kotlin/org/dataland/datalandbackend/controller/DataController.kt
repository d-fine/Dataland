package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataAPI
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity

/**
 * Implementation of the API for data exchange
 * @param dataManager implementation of the DataManagerInterface that defines how
 * Dataland handles data */

abstract class DataController<T>(
    var dataManager: DataManagerInterface,
    var objectMapper: ObjectMapper,
    private val clazz: Class<T>
) : DataAPI<T> {
    private val dataType = DataType(clazz.simpleName)
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun postCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<T>):
        ResponseEntity<DataMetaInformation> {
        logger.info(
            "Received a request to post company associated data " +
                "for companyId '${companyAssociatedData.companyId}'"
        )
        val dataIdOfPostedData = dataManager.addDataSet(
            StorableDataSet(
                companyAssociatedData.companyId, dataType,
                data = objectMapper.writeValueAsString(companyAssociatedData.data)
            )
        )
        return ResponseEntity.ok(
            DataMetaInformation(dataIdOfPostedData, dataType, companyAssociatedData.companyId)
        )
    }

    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<T>> {
        val companyId = dataManager.getDataMetaInfo(dataId).companyId
        logger.info("Received a request to get company data with dataId '$dataId' for companyId '$companyId'")
        return ResponseEntity.ok(
            CompanyAssociatedData(
                companyId = companyId,
                data = objectMapper.readValue(
                    dataManager.getDataSet(dataId, dataType).data,
                    clazz
                )
            )
        )
    }
}
