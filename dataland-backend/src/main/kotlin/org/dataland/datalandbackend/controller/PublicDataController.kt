package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.LogMessageBuilder
import org.dataland.datalandbackend.api.PublicDataApi
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.PublicDataManager
import org.dataland.datalandbackend.utils.canUserBypassQa
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import java.time.Instant
import java.util.UUID.randomUUID

/**
 * Abstract implementation of the controller for data exchange of an abstract type T
 * @param publicDataManager service to handle data
 * @param dataMetaInformationManager service for handling data meta information
 * @param objectMapper the mapper to transform strings into classes and vice versa
 */

abstract class PublicDataController<T>(
    var publicDataManager: PublicDataManager,
    var dataMetaInformationManager: DataMetaInformationManager,
    var objectMapper: ObjectMapper,
    private val clazz: Class<T>,
) : PublicDataApi<T> {
    private val dataType = DataType.of(clazz)
    private val logger = LoggerFactory.getLogger(javaClass)
    private val logMessageBuilder = LogMessageBuilder()

    override fun postCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<T>, bypassQa: Boolean):
        ResponseEntity<DataMetaInformation> {
        if (bypassQa && !canUserBypassQa(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(logMessageBuilder.bypassQaDeniedExceptionMessage)
        }
        val companyId = companyAssociatedData.companyId
        val reportingPeriod = companyAssociatedData.reportingPeriod
        val userId = DatalandAuthentication.fromContext().userId
        val uploadTime = Instant.now().toEpochMilli()
        logger.info(logMessageBuilder.postCompanyAssociatedDataMessage(userId, dataType, companyId, reportingPeriod))
        val correlationId = generateCorrelationId(companyAssociatedData.companyId)
        val datasetToStore = buildStorableDataset(companyAssociatedData, userId, uploadTime)
        val dataIdOfPostedData = publicDataManager.storeDataSetInMemoryAndSendReceptionMessageAndPersistMetaInfo(
            datasetToStore,
            bypassQa, correlationId,
        )
        logger.info(logMessageBuilder.postCompanyAssociatedDataSuccessMessage(companyId, correlationId))
        return ResponseEntity.ok(
            DataMetaInformation(
                dataId = dataIdOfPostedData, companyId = companyId, dataType = dataType,
                uploaderUserId = userId, uploadTime = uploadTime, reportingPeriod = reportingPeriod,
                currentlyActive = false, qaStatus = QaStatus.Pending,
            ),
        )
    }

    private fun buildStorableDataset(
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

    private fun generateCorrelationId(companyId: String): String {
        val correlationId = randomUUID().toString()
        logger.info(logMessageBuilder.generatedCorrelationIdMessage(correlationId, companyId))
        return correlationId
    }

    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<T>> {
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
        }
        val companyId = metaInfo.company.companyId
        val correlationId = generateCorrelationId(companyId)
        logger.info(logMessageBuilder.getCompanyAssociatedDataMessage(dataId, companyId))
        val companyAssociatedData = CompanyAssociatedData(
            companyId = companyId,
            reportingPeriod = metaInfo.reportingPeriod,
            data = objectMapper.readValue(publicDataManager.getDataSet(dataId, dataType, correlationId).data, clazz),
        )
        logger.info(
            logMessageBuilder.getCompanyAssociatedDataSuccessMessage(dataId, companyId, correlationId),
        )
        return ResponseEntity.ok(companyAssociatedData)
    }

    override fun getFrameworkDatasetsForCompany(
        companyId: String,
        showOnlyActive: Boolean,
        reportingPeriod: String?,
    ): ResponseEntity<List<DataAndMetaInformation<T>>> {
        val reportingPeriodInLog = reportingPeriod ?: "all reporting periods"
        logger.info(logMessageBuilder.getFrameworkDatasetsForCompanyMessage(dataType, companyId, reportingPeriodInLog))
        val metaInfos = dataMetaInformationManager.searchDataMetaInfo(
            companyId, dataType, showOnlyActive, reportingPeriod,
        )
        val authentication = DatalandAuthentication.fromContextOrNull()
        val listOfFrameworkDataAndMetaInfo = mutableListOf<DataAndMetaInformation<T>>()
        metaInfos.filter { it.isDatasetViewableByUser(authentication) }.forEach {
            val correlationId = generateCorrelationId(companyId)
            logger.info(logMessageBuilder.generatedCorrelationIdMessage(correlationId, companyId))
            val dataAsString = publicDataManager.getDataSet(
                it.dataId, DataType.valueOf(it.dataType),
                correlationId,
            ).data
            listOfFrameworkDataAndMetaInfo.add(
                DataAndMetaInformation(
                    it.toApiModel(DatalandAuthentication.fromContext()), objectMapper.readValue(dataAsString, clazz),
                ),
            )
        }
        return ResponseEntity.ok(listOfFrameworkDataAndMetaInfo)
    }
}
