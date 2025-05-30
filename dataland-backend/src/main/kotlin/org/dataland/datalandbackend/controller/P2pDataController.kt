package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.p2p.PathwaysToParisData
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackendutils.model.ExportFileType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the P2P framework endpoints
 * @param datasetStorageService data manager to be used
 * @param dataMetaInformationManager metadata manager
 * @param dataExportService export service
 * @param companyQueryManager company query manager
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/p2p")
@RestController
class P2pDataController
    @Autowired
    constructor(
        datasetStorageService: DataManager,
        dataMetaInformationManager: DataMetaInformationManager,
        dataExportService: DataExportService,
        companyQueryManager: CompanyQueryManager,
        objectMapper: ObjectMapper,
    ) : DataController<PathwaysToParisData>(
            datasetStorageService,
            dataMetaInformationManager,
            dataExportService,
            objectMapper,
            companyQueryManager,
            PathwaysToParisData::class.java,
        ) {
        @Operation(operationId = "getCompanyAssociatedP2pData")
        override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<PathwaysToParisData>> =
            super.getCompanyAssociatedData(dataId)

        @Operation(operationId = "getCompanyAssociatedP2pDataByDimensions")
        override fun getCompanyAssociatedDataByDimensions(
            reportingPeriod: String,
            companyId: String,
        ): ResponseEntity<CompanyAssociatedData<PathwaysToParisData>> =
            super
                .getCompanyAssociatedDataByDimensions(reportingPeriod, companyId)

        @Operation(operationId = "postCompanyAssociatedP2pData")
        override fun postCompanyAssociatedData(
            companyAssociatedData: CompanyAssociatedData<PathwaysToParisData>,
            bypassQa: Boolean,
        ): ResponseEntity<DataMetaInformation> = super.postCompanyAssociatedData(companyAssociatedData, bypassQa)

        @Operation(operationId = "getAllCompanyP2pData")
        override fun getFrameworkDatasetsForCompany(
            companyId: String,
            showOnlyActive: Boolean,
            reportingPeriod: String?,
        ): ResponseEntity<List<DataAndMetaInformation<PathwaysToParisData>>> =
            super.getFrameworkDatasetsForCompany(companyId, showOnlyActive, reportingPeriod)

        @Operation(operationId = "exportCompanyAssociatedP2pDataByDimensions")
        override fun exportCompanyAssociatedDataByDimensions(
            reportingPeriods: List<String>,
            companyIds: List<String>,
            exportFileType: ExportFileType,
            includeDataMetaInformation: Boolean,
        ): ResponseEntity<InputStreamResource> =
            super.exportCompanyAssociatedDataByDimensions(
                reportingPeriods,
                companyIds,
                exportFileType,
                includeDataMetaInformation,
            )
    }
