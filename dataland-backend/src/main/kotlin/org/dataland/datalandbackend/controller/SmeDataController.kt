package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.api.SmeDataApi
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.PrivateDataManager
import org.dataland.datalandbackend.utils.IdUtils.generateUUID
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.*

/**
 * Controller for the SME framework endpoints
 * @param privateDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RestController
class SmeDataController(
    @Autowired var privateDataManager: PrivateDataManager,
    @Autowired var myObjectMapper: ObjectMapper,
    @Autowired var logMessageBuilder: LogMessageBuilder,
    @Autowired var dataMetaInformationManager: DataMetaInformationManager,
) : SmeDataApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "postSmeJsonAndDocuments")
    override fun postSmeJsonAndDocuments(
        companyAssociatedSmeData: CompanyAssociatedData<SmeData>,
        documents: Array<MultipartFile>?,
    ): ResponseEntity<DataMetaInformation> {
        val dataMetaInformation = privateDataManager.processPrivateSmeDataStorageRequest(
            companyAssociatedSmeData,
            documents,
        )
        return ResponseEntity.ok(dataMetaInformation)
    }

    @Operation(operationId = "getCompanyAssociatedSmeData")
    override fun getCompanyAssociatedSmeData(dataId: String): ResponseEntity<CompanyAssociatedData<SmeData>> {
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
        }
        val companyId = metaInfo.company.companyId
        val correlationId = generateUUID()
        logger.info(logMessageBuilder.getCompanyAssociatedDataMessage(dataId, companyId))
        val companyAssociatedData = CompanyAssociatedData(
            companyId = companyId,
            reportingPeriod = metaInfo.reportingPeriod,
            data = privateDataManager.getPrivateSmeData(dataId, correlationId),
        )
        logger.info(
            logMessageBuilder.getCompanyAssociatedDataSuccessMessage(dataId, companyId, correlationId),
        )
        return ResponseEntity.ok(companyAssociatedData)
    }

    @Operation(operationId = "getPrivateDocument")
    override fun getPrivateDocument(dataId: String, hash: String): ResponseEntity<InputStreamResource> {
        val correlationId = generateUUID()
        val document = privateDataManager.retrievePrivateDocumentById(dataId, hash, correlationId)
        return ResponseEntity.ok()
            .contentType(document.type.mediaType)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=${document.documentId}.${document.type.fileExtension}",
            )
            .body(document.content)
    }

    @Operation(operationId = "getFrameworkDatasetsForCompany")
    override fun getFrameworkDatasetsForCompany(
        // TODO this function is mostly duplicate code to the code in DataController.kt => think about it later
        companyId: String,
        showOnlyActive: Boolean,
        reportingPeriod: String?,
    ): ResponseEntity<List<DataAndMetaInformation<SmeData>>> {
        val reportingPeriodInLog = reportingPeriod ?: "all reporting periods"
        val smeDataType = DataType.of(SmeData::class.java)
        logger.info(
            logMessageBuilder.getFrameworkDatasetsForCompanyMessage(smeDataType, companyId, reportingPeriodInLog),
        )
        val metaInfos = dataMetaInformationManager.searchDataMetaInfo(
            companyId, smeDataType, showOnlyActive, reportingPeriod,
        )
        val authentication = DatalandAuthentication.fromContextOrNull()
        val frameworkDataAndMetaInfo = mutableListOf<DataAndMetaInformation<SmeData>>()
        metaInfos.filter { it.isDatasetViewableByUser(authentication) }.forEach {
            val correlationId = generateCorrelationId(companyId)
            val smeData = privateDataManager.getPrivateSmeData(it.dataId, correlationId)
            frameworkDataAndMetaInfo.add(
                DataAndMetaInformation(
                    it.toApiModel(DatalandAuthentication.fromContext()), smeData,
                ),
            )
        }
        return ResponseEntity.ok(frameworkDataAndMetaInfo)
    }

    private fun generateCorrelationId(companyId: String): String {
        // TODO this function is mostly duplicate code, see DataController.kt => handle this problem later
        val correlationId = UUID.randomUUID().toString()
        logger.info(logMessageBuilder.generatedCorrelationIdMessage(correlationId, companyId))
        return correlationId
    }
}
