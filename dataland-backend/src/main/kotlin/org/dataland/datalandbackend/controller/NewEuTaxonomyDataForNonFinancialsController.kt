package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.DataAndMetaInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.eutaxonomy.nonfinancials.NewEuTaxonomyDataForNonFinancials
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
@RequestMapping("/data/new-eutaxonomy-non-financials")
@RestController
class NewEuTaxonomyDataForNonFinancialsController(
    @Autowired var myDataManager: DataManager,
    @Autowired var myMetaDataManager: DataMetaInformationManager,
    @Autowired var myObjectMapper: ObjectMapper,
) : DataController<NewEuTaxonomyDataForNonFinancials>(
    myDataManager,
    myMetaDataManager,
    myObjectMapper,
    NewEuTaxonomyDataForNonFinancials::class.java,
) {
    @Operation(operationId = "getCompanyAssociatedNewEuTaxonomyDataForNonFinancials")
    override fun getCompanyAssociatedData(dataId: String):
        ResponseEntity<CompanyAssociatedData<NewEuTaxonomyDataForNonFinancials>> {
        return super.getCompanyAssociatedData(dataId)
    }

    @Operation(operationId = "postCompanyAssociatedNewEuTaxonomyDataForNonFinancials")
    override fun postCompanyAssociatedData(
        companyAssociatedData: CompanyAssociatedData<NewEuTaxonomyDataForNonFinancials>,
        bypassQa: Boolean,
    ): ResponseEntity<DataMetaInformation> {
        return super.postCompanyAssociatedData(companyAssociatedData, bypassQa)
    }

    @Operation(operationId = "getAllCompanyNewEuTaxonomyDataForNonFinancials")
    override fun getFrameworkDatasetsForCompany(
        companyId: String,
        showOnlyActive: Boolean,
        reportingPeriod: String?,
    ): ResponseEntity<List<DataAndMetaInformation<NewEuTaxonomyDataForNonFinancials>>> {
        return super.getFrameworkDatasetsForCompany(companyId, showOnlyActive, reportingPeriod)
    }
}
