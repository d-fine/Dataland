package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import org.dataland.datalandbackend.model.CompanyAssociatedData
import org.dataland.datalandbackend.model.DataAndMetaInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.eutaxonomy.financials.EuTaxonomyDataForFinancials
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the EU Taxonomy endpoints of financial companies
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/eutaxonomy-financials")
@RestController
class EuTaxonomyDataForFinancialsController(
    @Autowired var myDataManager: DataManager,
    @Autowired var myMetaDataManager: DataMetaInformationManager,
    @Autowired var myObjectMapper: ObjectMapper,
) : DataController<EuTaxonomyDataForFinancials>(
    myDataManager,
    myMetaDataManager,
    myObjectMapper,
    EuTaxonomyDataForFinancials::class.java,
) {
    @Operation(operationId = "getCompanyAssociatedEuTaxonomyDataForFinancials")
    override fun getCompanyAssociatedData(dataId: String):
        ResponseEntity<CompanyAssociatedData<EuTaxonomyDataForFinancials>> {
        return super.getCompanyAssociatedData(dataId)
    }

    @Operation(operationId = "postCompanyAssociatedEuTaxonomyDataForFinancials")
    override fun postCompanyAssociatedData(companyAssociatedData: CompanyAssociatedData<EuTaxonomyDataForFinancials>):
        ResponseEntity<DataMetaInformation> {
        return super.postCompanyAssociatedData(companyAssociatedData)
    }

    @Operation(operationId = "getAllCompanyEuTaxonomyDataForFinancials")
    override fun getFrameworkDatasetsForCompany(companyId: String, showVersionHistoryForReportingPeriod: Boolean, reportingPeriod: String?):
        ResponseEntity<List<DataAndMetaInformation<EuTaxonomyDataForFinancials>>> {
        return super.getFrameworkDatasetsForCompany(companyId, showVersionHistoryForReportingPeriod, reportingPeriod)
    }
}
