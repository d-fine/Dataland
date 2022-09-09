package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyAPI
import org.dataland.datalandbackend.interfaces.CompanyManagerInterface
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.enums.company.StockIndex
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the API for company data exchange
 * @param companyManager implementation of the DataManagerInterface that defines how
 * Dataland handles data
 */

@RestController
class CompanyDataController(
    @Autowired var companyManager: CompanyManagerInterface,
) : CompanyAPI {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun postCompany(companyInformation: CompanyInformation): ResponseEntity<StoredCompany> {
        logger.info("Received a request to post a company with name '${companyInformation.companyName}'")
        return ResponseEntity.ok(companyManager.addCompany(companyInformation).toApiModel())
    }

    @Transactional
    override fun getCompanies(
        searchString: String?,
        stockIndices: Set<StockIndex>?,
        dataTypes: Set<DataType>?,
        onlyCompanyNames: Boolean
    ): ResponseEntity<List<StoredCompany>> {
        logger.info(
            "Received a request to get companies with " +
                "searchString='$searchString', onlyCompanyNames='$onlyCompanyNames', dataTypes='$dataTypes', "
        )
        return ResponseEntity.ok(
            companyManager.searchCompanies(
                searchString ?: "",
                onlyCompanyNames,
                dataTypes ?: setOf(),
                stockIndices ?: setOf()
            ).map { it.toApiModel() }
        )
    }

    override fun getCompanyById(companyId: String): ResponseEntity<StoredCompany> {
        return ResponseEntity.ok(companyManager.getCompanyById(companyId).toApiModel())
    }

    override fun getTeaserCompanies(): List<String> {
        return companyManager.getTeaserCompanyIds()
    }
}
