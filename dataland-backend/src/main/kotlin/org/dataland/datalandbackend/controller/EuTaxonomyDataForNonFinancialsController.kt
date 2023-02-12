package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.DataAndMetaInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.eutaxonomy.nonfinancials.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the EU Taxonomy endpoints of non-financial companies
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/eutaxonomy-non-financials")
@RestController
class EuTaxonomyDataForNonFinancialsController(
    @Autowired var myDataManager: DataManager,
    @Autowired var myMetaDataManager: DataMetaInformationManager,
    @Autowired var myObjectMapper: ObjectMapper
) : DataController<EuTaxonomyDataForNonFinancials>(
    myDataManager,
    myMetaDataManager,
    myObjectMapper,
    EuTaxonomyDataForNonFinancials::class.java
) {
    @Operation(operationId = "getCompanyAssociatedEuTaxonomyDataForNonFinancials")
    override fun getCompanyAssociatedData(dataId: String):
        ResponseEntity<CompanyAssociatedData<EuTaxonomyDataForNonFinancials>> {
        return super.getCompanyAssociatedData(dataId)
    }

    @Operation(operationId = "postCompanyAssociatedEuTaxonomyDataForNonFinancials")
    override fun postCompanyAssociatedData(
        companyAssociatedData: CompanyAssociatedData<EuTaxonomyDataForNonFinancials>
    ): ResponseEntity<DataMetaInformation> {
        return super.postCompanyAssociatedData(companyAssociatedData)
    }

    @Operation(operationId = "getAllCompanyEuTaxonomyDataForNonFinancials")
    override fun getAllCompanyData(companyId: String):
        ResponseEntity<List<DataAndMetaInformation<EuTaxonomyDataForNonFinancials>>> {
        return super.getAllCompanyData(companyId)
    }
}
