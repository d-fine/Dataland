package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.p2p.PathwaysToParisData
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the P2P framework endpoints
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/p2p")
@RestController
class P2pDataController(
    @Autowired var myDataManager: DataManager,
    @Autowired var myMetaDataManager: DataMetaInformationManager,
    @Autowired var myDataExportService: DataExportService,
    @Autowired var myObjectMapper: ObjectMapper,
) : DataController<PathwaysToParisData>(
        myDataManager,
        myMetaDataManager,
        myDataExportService,
        myObjectMapper,
        PathwaysToParisData::class.java,
    ) {
    @Operation(operationId = "getCompanyAssociatedP2pData")
    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<PathwaysToParisData>> =
        super.getCompanyAssociatedData(dataId)

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
        super
            .getFrameworkDatasetsForCompany(companyId, showOnlyActive, reportingPeriod)

    @Operation(operationId = "exportCompanyAssociatedP2pDataToJson")
    override fun exportCompanyAssociatedDataToJson(dataId: String): ResponseEntity<InputStreamResource> =
        super.exportCompanyAssociatedDataToJson(dataId)

    @Operation(operationId = "exportCompanyAssociatedP2pDataToCsv")
    override fun exportCompanyAssociatedDataToCsv(dataId: String): ResponseEntity<InputStreamResource> =
        super.exportCompanyAssociatedDataToCsv(dataId)

    @Operation(operationId = "exportCompanyAssociatedP2pDataToExcel")
    override fun exportCompanyAssociatedDataToExcel(dataId: String): ResponseEntity<InputStreamResource> =
        super.exportCompanyAssociatedDataToExcel(dataId)
}
