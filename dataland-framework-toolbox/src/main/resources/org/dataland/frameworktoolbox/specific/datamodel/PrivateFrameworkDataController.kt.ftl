// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.${frameworkPackageName}

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.api.${frameworkDataType.shortenedQualifier}Api
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.PrivateDataManager
import org.dataland.datalandbackend.utils.IdUtils.generateCorrelationId
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
<#list frameworkDataType.imports as import>import ${import}
</#list>

/**
 * Controller for the ${frameworkIdentifier} framework endpoints
 * @param privateDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RestController
class ${frameworkDataType.shortenedQualifier}Controller(
    @Autowired var privateDataManager: PrivateDataManager,
    @Autowired var myObjectMapper: ObjectMapper,
    @Autowired var logMessageBuilder: LogMessageBuilder,
    @Autowired var dataMetaInformationManager: DataMetaInformationManager,
) : ${frameworkDataType.shortenedQualifier}Api {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "post${frameworkIdentifier?cap_first}JsonAndDocuments")
    override fun post${frameworkIdentifier?cap_first}JsonAndDocuments(
            companyAssociated${frameworkDataType.shortenedQualifier}: CompanyAssociatedData<${frameworkDataType.shortenedQualifier}>,
            documents: Array<MultipartFile>?,
        ):
        ResponseEntity<DataMetaInformation> {
        val dataMetaInformation = privateDataManager.processPrivate${frameworkDataType.shortenedQualifier}StorageRequest(
            companyAssociated${frameworkDataType.shortenedQualifier},
            documents,
        )
        return ResponseEntity.ok(dataMetaInformation)
    }

    @Operation(operationId = "getCompanyAssociated${frameworkDataType.shortenedQualifier}")
    override fun getCompanyAssociated${frameworkDataType.shortenedQualifier}(dataId: String):
        ResponseEntity<CompanyAssociatedData<${frameworkDataType.shortenedQualifier}>> {
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
        }
        val companyId = metaInfo.company.companyId
        val correlationId = generateCorrelationId(companyId = companyId, dataId = dataId)
        logger.info(logMessageBuilder.getCompanyAssociatedDataMessage(dataId, companyId))
        val companyAssociatedData = CompanyAssociatedData(
            companyId = companyId,
            reportingPeriod = metaInfo.reportingPeriod,
            data = privateDataManager.getPrivate${frameworkDataType.shortenedQualifier}(dataId, correlationId),
        )
        logger.info(
            logMessageBuilder.getCompanyAssociatedDataSuccessMessage(dataId, companyId, correlationId),
        )
        return ResponseEntity.ok(companyAssociatedData)
    }

    @Operation(operationId = "getPrivateDocument")
    override fun getPrivateDocument(
            dataId: String,
            hash: String):
        ResponseEntity<InputStreamResource> {
        val correlationId = generateCorrelationId(companyId = null, dataId = dataId)
        val document = privateDataManager.retrievePrivateDocumentById(dataId, hash, correlationId)
        return ResponseEntity.ok()
            .contentType(document.type.mediaType)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename= <#noparse>${document.documentId}.${document.type.fileExtension}</#noparse>",
            )
            .body(document.content)
        }

    @Operation(operationId = "getFrameworkDatasetsForCompany")
    override fun getFrameworkDatasetsForCompany(
            companyId: String,
            showOnlyActive: Boolean,
            reportingPeriod: String?,
        ):
        ResponseEntity<List<DataAndMetaInformation<${frameworkDataType.shortenedQualifier}>>> {
            val reportingPeriodInLog = reportingPeriod ?: "all reporting periods"
            val dataType = DataType.of(${frameworkDataType.shortenedQualifier}::class.java)
            logger.info(
                logMessageBuilder.getFrameworkDatasetsForCompanyMessage(dataType, companyId, reportingPeriodInLog),
            )
            val metaInfos = dataMetaInformationManager.searchDataMetaInfo(
                companyId, dataType, showOnlyActive, reportingPeriod,
            )
            val authentication = DatalandAuthentication.fromContextOrNull()
            val frameworkDataAndMetaInfo = mutableListOf<DataAndMetaInformation<${frameworkDataType.shortenedQualifier}>>()
            metaInfos.filter { it.isDatasetViewableByUser(authentication) }.forEach {
                val correlationId = generateCorrelationId(companyId = companyId, dataId = null)
                val data = privateDataManager.getPrivate${frameworkIdentifier?cap_first}Data(it.dataId, correlationId)
                frameworkDataAndMetaInfo.add(
                    DataAndMetaInformation(
                        it.toApiModel(DatalandAuthentication.fromContext()), data,
                    ),
                )
            }
        return ResponseEntity.ok(frameworkDataAndMetaInfo)
        }
}