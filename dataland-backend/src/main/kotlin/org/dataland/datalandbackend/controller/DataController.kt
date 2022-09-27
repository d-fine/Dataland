package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataAPI
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.interfaces.DataMetaInformationManagerInterface
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import java.util.UUID.randomUUID

/**
 * Implementation of the API for data exchange
 * @param dataManager implementation of the DataManagerInterface that defines how
 * Dataland handles data */

abstract class DataController<T>(
    var dataManager: DataManagerInterface,
    var dataMetaInformationManager: DataMetaInformationManagerInterface,
    var objectMapper: ObjectMapper,
    private val clazz: Class<T>,
) : DataAPI<T> {
    private val dataType = DataType.of(clazz)
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun postCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<T>):
        ResponseEntity<DataMetaInformation> {
        logger.info(
            "Received a request to post company associated data of type $dataType" +
                "for companyId '${companyAssociatedData.companyId}'"
        )
        val correlationId = generatedCorrelationId(companyAssociatedData.companyId)
        val dataIdOfPostedData = dataManager.addDataSet(
            StorableDataSet(
                companyAssociatedData.companyId, dataType,
                data = objectMapper.writeValueAsString(companyAssociatedData.data),
            ),
            correlationId
        )
        logger.info(
            "Posted company associated data for companyId '${companyAssociatedData.companyId}'. " +
                "Correlation ID: $correlationId"
        )
        return ResponseEntity.ok(DataMetaInformation(dataIdOfPostedData, dataType, companyAssociatedData.companyId))
    }

    private fun generatedCorrelationId(companyId: String): String {
        val correlationId = randomUUID().toString()
        logger.info(
            "Generated correlation ID '$correlationId' for the received request with company ID: $companyId."
        )
        return correlationId
    }

    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<T>> {
        val companyId = dataMetaInformationManager.getDataMetaInformationByDataId(dataId).company.companyId
        val correlationId = generatedCorrelationId(companyId)
        logger.info(
            "Received a request to get company data with dataId '$dataId' for companyId '$companyId'. "
        )
        val companyAssociatedData = CompanyAssociatedData(
            companyId = companyId,
            data = objectMapper.readValue(
                dataManager.getDataSet(dataId, dataType, correlationId).data,
                clazz
            ),
        )
        logger.info(
            "Received company data with dataId '$dataId' for companyId '$companyId' from EuroDaT. " +
                "Correlation ID '$correlationId'"
        )
        return ResponseEntity.ok(companyAssociatedData)
    }
}
