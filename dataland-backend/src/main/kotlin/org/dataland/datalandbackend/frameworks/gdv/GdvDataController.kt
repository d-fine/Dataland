package org.dataland.datalandbackend.frameworks.gdv

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.controller.DataController
import org.dataland.datalandbackend.frameworks.gdv.model.GdvData
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the Gdv framework endpoints
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/gdv")
@RestController
class GdvDataController(
    @Autowired var myDataManager: DataManager,
    @Autowired var myMetaDataManager: DataMetaInformationManager,
    @Autowired var myObjectMapper: ObjectMapper,
) : DataController<GdvData>(
    myDataManager,
    myMetaDataManager,
    myObjectMapper,
    GdvData::class.java,
) {
    @Operation(operationId = "getCompanyAssociatedGdvData")
    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<GdvData>> {
        return super.getCompanyAssociatedData(dataId)
    }

    @Operation(operationId = "postCompanyAssociatedGdvData")
    override fun postCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<GdvData>, bypassQa: Boolean):
        ResponseEntity<DataMetaInformation> {
        return super.postCompanyAssociatedData(companyAssociatedData, bypassQa)
    }

    @Operation(operationId = "getAllCompanyGdvData")
    override fun getFrameworkDatasetsForCompany(
        companyId: String,
        showOnlyActive: Boolean,
        reportingPeriod: String?,
    ): ResponseEntity<List<DataAndMetaInformation<GdvData>>> {
        return super.getFrameworkDatasetsForCompany(companyId, showOnlyActive, reportingPeriod)
    }
}
