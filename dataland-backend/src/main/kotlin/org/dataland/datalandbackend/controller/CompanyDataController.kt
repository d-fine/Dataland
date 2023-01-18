package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyApi
import org.dataland.datalandbackend.model.CompanyAvailableDistinctValues
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.CompanySearchFilter
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.services.CompanyManager
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the company data endpoints
 * @param companyManager the company manager service to handle company information
 */

@RestController
class CompanyDataController(
    @Autowired var companyManager: CompanyManager,
) : CompanyApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun postCompany(companyInformation: CompanyInformation): ResponseEntity<StoredCompany> {
        logger.info("Received a request to post a company with name '${companyInformation.companyName}'")
        return ResponseEntity.ok(
            companyManager.addCompany(companyInformation)
                .toApiModel(DatalandAuthentication.fromContext())
        )
    }

    override fun getCompanies(
        searchString: String?,
        dataTypes: Set<DataType>?,
        countryCodes: Set<String>?,
        sectors: Set<String>?,
        onlyCompanyNames: Boolean
    ): ResponseEntity<List<StoredCompany>> {
        logger.info(
            "Received a request to get companies with " +
                "searchString='$searchString', onlyCompanyNames='$onlyCompanyNames', dataTypes='$dataTypes', " +
                "countryCodes='$countryCodes', sectors='$sectors'"
        )
        return ResponseEntity.ok(
            companyManager.searchCompaniesAndGetApiModel(
                CompanySearchFilter(
                    searchString ?: "",
                    onlyCompanyNames,
                    dataTypes ?: setOf(),
                    countryCodes ?: setOf(),
                    sectors ?: setOf(),
                ),
                DatalandAuthentication.fromContextOrNull()
            )
        )
    }

    override fun getAvailableCompanySearchFilters(): ResponseEntity<CompanyAvailableDistinctValues> {
        return ResponseEntity.ok(
            CompanyAvailableDistinctValues(
                countryCodes = companyManager.getDistinctCountryCodes(),
                sectors = companyManager.getDistinctSectors(),
            )
        )
    }

    override fun getCompanyById(companyId: String): ResponseEntity<StoredCompany> {
        return ResponseEntity.ok(
            companyManager
                .getCompanyApiModelById(companyId, DatalandAuthentication.fromContextOrNull())
        )
    }

    override fun getCompanyFrameworkDataById(companyId: String, dataType: DataType):
        ResponseEntity<List<Map<String, Any>>> {
        return ResponseEntity.ok(companyManager.getCompanyFrameworkDataById(companyId, dataType))
    }

    override fun getTeaserCompanies(): List<String> {
        return companyManager.getTeaserCompanyIds()
    }
}
