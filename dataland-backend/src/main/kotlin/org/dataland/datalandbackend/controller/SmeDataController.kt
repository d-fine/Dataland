package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.LogMessageBuilder
import org.dataland.datalandbackend.api.SmeDataApi
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.DataMetaInformationManager
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

/**
 * Controller for the SME framework endpoints
 * @param privateDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RestController
class SmeDataController(
    @Autowired var privateDataManager: PrivateDataManager,
    @Autowired var myObjectMapper: ObjectMapper,
    var dataMetaInformationManager: DataMetaInformationManager,
) : SmeDataApi {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val logMessageBuilder = LogMessageBuilder()

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
            data = myObjectMapper.readValue(
                privateDataManager.getPrivateDataSet(dataId, correlationId).data,
                SmeData::class.java,
            ),
        )
        logger.info(
            logMessageBuilder.getCompanyAssociatedDataSuccessMessage(dataId, companyId, correlationId),
        )
        return ResponseEntity.ok(companyAssociatedData)
    }
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
}
