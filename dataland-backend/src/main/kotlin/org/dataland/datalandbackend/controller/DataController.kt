package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataApi
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.DataAndMetaInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.enums.data.QAStatus
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import java.time.Instant
import java.util.UUID.randomUUID

/**
 * Abstract implementation of the controller for data exchange of an abstract type T
 * @param dataManager service to handle data
 * @param dataMetaInformationManager service for handling data meta information
 * @param objectMapper the mapper to transform strings into classes and vice versa
 */

abstract class DataController<T>(
    var dataManager: DataManager,
    var dataMetaInformationManager: DataMetaInformationManager,
    var objectMapper: ObjectMapper,
    private val clazz: Class<T>,
) : DataApi<T> {
    private val dataType = DataType.of(clazz)
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun postCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<T>):
        ResponseEntity<DataMetaInformation> {
        val companyId = companyAssociatedData.companyId
        val reportingPeriod = companyAssociatedData.reportingPeriod
        val userId = DatalandAuthentication.fromContext().userId
        val uploadTime = Instant.now().epochSecond
        logger.info(
            "Received a request from user '$userId' to post company associated data of type $dataType " +
                "for company ID '$companyId' and the reporting period $reportingPeriod",
        )
        val correlationId = generatedCorrelationId(companyId)
        val datasetToStore = buildDatasetToStore(companyAssociatedData, userId, uploadTime)
        val dataIdOfPostedData = dataManager.addDataSetToTemporaryStorageAndSendMessage(datasetToStore, correlationId)
        logger.info("Posted company associated data for companyId '$companyId'. Correlation ID: $correlationId")
        return ResponseEntity.ok(
            DataMetaInformation(
                dataId = dataIdOfPostedData,
                companyId = companyId,
                dataType = dataType,
                uploaderUserId = userId,
                uploadTime = uploadTime,
                reportingPeriod = reportingPeriod,
                currentlyActive = false,
                qaStatus = QAStatus.Pending,
            ),
        )
    }

    private fun buildDatasetToStore(
        companyAssociatedData: CompanyAssociatedData<T>,
        userId: String,
        uploadTime: Long,
    ): StorableDataSet {
        return StorableDataSet(
            companyId = companyAssociatedData.companyId,
            dataType = dataType,
            uploaderUserId = userId,
            uploadTime = uploadTime,
            reportingPeriod = companyAssociatedData.reportingPeriod,
            data = objectMapper.writeValueAsString(companyAssociatedData.data),
        )
    }

    private fun generatedCorrelationId(companyId: String): String {
        val correlationId = randomUUID().toString()
        logger.info(
            "Generated correlation ID '$correlationId' for the received request with company ID: $companyId.",
        )
        return correlationId
    }

    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<T>> {
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException("You are trying to access a unreviewed dataset")
        }
        val companyId = metaInfo.company.companyId
        val reportingPeriod = metaInfo.reportingPeriod
        val correlationId = generatedCorrelationId(companyId)
        logger.info(
            "Received a request to get company data with dataId '$dataId' for companyId '$companyId'. ",
        )
        val companyAssociatedData = CompanyAssociatedData(
            companyId = companyId,
            reportingPeriod = reportingPeriod,
            data = objectMapper.readValue(dataManager.getDataSet(dataId, dataType, correlationId).data, clazz),
        )
        logger.info(
            "Received company data with dataId '$dataId' for companyId '$companyId' from framework data storage. " +
                "Correlation ID '$correlationId'",
        )
        return ResponseEntity.ok(companyAssociatedData)
    }

    override fun getFrameworkDatasetsForCompany(
        companyId: String,
        showOnlyActive: Boolean,
        reportingPeriod: String?,
    ): ResponseEntity<List<DataAndMetaInformation<T>>> {
        val reportingPeriodInLogMessage = reportingPeriod ?: "all reporting periods"
        logger.info(
            "Received a request to get all datasets together with meta info for framework '$dataType', " +
                "companyId '$companyId' and reporting period '$reportingPeriodInLogMessage'",
        )
        val metaInfos = dataMetaInformationManager.searchDataMetaInfo(
            companyId,
            dataType,
            showOnlyActive,
            reportingPeriod,
        )
        val authentication = DatalandAuthentication.fromContextOrNull()
        val listOfFrameworkDataAndMetaInfo = mutableListOf<DataAndMetaInformation<T>>()
        metaInfos
            .filter { it.isDatasetViewableByUser(authentication) }
            .forEach {
                val correlationId = generatedCorrelationId(companyId)
                val dataAsString = dataManager.getDataSet(it.dataId, DataType.valueOf(it.dataType), correlationId).data
                listOfFrameworkDataAndMetaInfo.add(
                    DataAndMetaInformation(
                        it.toApiModel(DatalandAuthentication.fromContext()),
                        objectMapper.readValue(dataAsString, clazz),
                    ),
                )
            }
        return ResponseEntity.ok(listOfFrameworkDataAndMetaInfo)
    }
}
