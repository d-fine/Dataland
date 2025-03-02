// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.heimathafen

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.controller.DataController
import org.dataland.datalandbackend.frameworks.heimathafen.model.HeimathafenData
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
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
 * Controller for the heimathafen framework endpoints
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/heimathafen")
@RestController
class HeimathafenDataController(
    @Autowired var myDataManager: DataManager,
    @Autowired var myMetaDataManager: DataMetaInformationManager,
    @Autowired var myDataExportService: DataExportService,
    @Autowired var myObjectMapper: ObjectMapper,
) : DataController<HeimathafenData>(
        myDataManager,
        myMetaDataManager,
        myDataExportService,
        myObjectMapper,
        HeimathafenData::class.java,
    ) {
    @Operation(operationId = "getCompanyAssociatedHeimathafenData")
    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<HeimathafenData>> =
        super.getCompanyAssociatedData(dataId)

    @Operation(operationId = "getCompanyAssociatedHeimathafenDataByDimensions")
    override fun getCompanyAssociatedDataByDimensions(
        reportingPeriod: String,
        companyId: String,
    ): ResponseEntity<CompanyAssociatedData<HeimathafenData>> = super.getCompanyAssociatedDataByDimensions(reportingPeriod, companyId)

    @Operation(operationId = "postCompanyAssociatedHeimathafenData")
    override fun postCompanyAssociatedData(
        companyAssociatedData: CompanyAssociatedData<HeimathafenData>,
        bypassQa: Boolean,
    ): ResponseEntity<DataMetaInformation> = super.postCompanyAssociatedData(companyAssociatedData, bypassQa)

    @Operation(operationId = "exportCompanyAssociatedHeimathafenDataByDimensions")
    override fun exportCompanyAssociatedDataByDimensions(
        reportingPeriod: String,
        companyId: String,
        exportFileType: ExportFileType,
    ): ResponseEntity<InputStreamResource> = super.exportCompanyAssociatedDataByDimensions(reportingPeriod, companyId, exportFileType)

    @Operation(operationId = "getAllCompanyHeimathafenData")
    override fun getFrameworkDatasetsForCompany(
        companyId: String,
        showOnlyActive: Boolean,
        reportingPeriod: String?,
    ): ResponseEntity<List<DataAndMetaInformation<HeimathafenData>>> =
        super
            .getFrameworkDatasetsForCompany(companyId, showOnlyActive, reportingPeriod)
}
