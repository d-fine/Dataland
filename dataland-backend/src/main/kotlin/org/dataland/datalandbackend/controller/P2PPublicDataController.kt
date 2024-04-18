package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.p2p.PathwaysToParisData
import org.dataland.datalandbackend.services.PublicDataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the P2P framework endpoints
 * @param myPublicDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/p2p")
@RestController
class P2PPublicDataController(
    @Autowired var myPublicDataManager: PublicDataManager,
    @Autowired var myMetaDataManager: DataMetaInformationManager,
    @Autowired var myObjectMapper: ObjectMapper,
) : PublicDataController<PathwaysToParisData>(
    myPublicDataManager,
    myMetaDataManager,
    myObjectMapper,
    PathwaysToParisData::class.java,
) {
    @Operation(operationId = "getCompanyAssociatedP2pData")
    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<PathwaysToParisData>> {
        return super.getCompanyAssociatedData(dataId)
    }

    @Operation(operationId = "postCompanyAssociatedP2pData")
    override fun postCompanyAssociatedData(
        companyAssociatedData: CompanyAssociatedData<PathwaysToParisData>,
        bypassQa: Boolean,
    ):
        ResponseEntity<DataMetaInformation> {
        return super.postCompanyAssociatedData(companyAssociatedData, bypassQa)
    }

    @Operation(operationId = "getAllCompanyP2pData")
    override fun getFrameworkDatasetsForCompany(
        companyId: String,
        showOnlyActive: Boolean,
        reportingPeriod: String?,
    ): ResponseEntity<List<DataAndMetaInformation<PathwaysToParisData>>> {
        return super.getFrameworkDatasetsForCompany(companyId, showOnlyActive, reportingPeriod)
    }
}
