package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandbackend.model.CompaniesRequestBody
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataIdentifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.validation.Valid

/**
 * Defines the restful dataland-backend API regarding company data.
 */

@RequestMapping("/")
interface CompanyAPI {

    @Operation(
        summary = "Add a new company.",
        description = "A new company is added using the provided information, the generated company ID is returned."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added company.")
        ]
    )
    @PostMapping(
        value = ["/companies"],
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    /**
     * A method to create a new company entry in dataland
     * @param companiesRequestBody includes the company name of the company to be created
     * @return meta information about the stored company (id and company name)
     */
    fun postCompany(@Valid @RequestBody companiesRequestBody: CompaniesRequestBody): ResponseEntity<CompanyMetaInformation>

    @Operation(
        summary = "Retrieve specific companies by name or just all companies from the data store.",
        description = "Companies identified via the provided company name are retrieved. " +
            "If company name is an empty string, all companies in the data store are returned."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved a map of companies.")
        ]
    )
    @GetMapping(
        value = ["/companies"],
        produces = ["application/json"]
    )
    /**
     * A method to retrieve specific companies identified by their company names
     * @param companyName identifier used to search for companies in the data store
     * @return all companies whose names match with the companyName provided as search input
     */
    fun getCompaniesByName(@RequestParam companyName: String? = null):
        ResponseEntity<List<CompanyMetaInformation>>

    @Operation(
        summary = "Retrieve list of existing data sets for given company.",
        description = "A List of data ID and data type of all data sets of the given company is retrieved."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved list of data sets.")
        ]
    )
    @GetMapping(
        value = ["/companies/{companyId}/data"],
        produces = ["application/json"]
    )
    /**
     * A method to retrieve all existing data sets of a specific company identified by the company ID
     * @param companyId identifier of the company in dataland
     * @return list of data identifiers (data ID and data type) of all existing data sets of the specified company
     */
    fun getCompanyDataSets(@PathVariable("companyId") companyId: String): ResponseEntity<List<DataIdentifier>>

    @Operation(
        summary = "Retrieve company meta information.",
        description = "The company meta information behind the given company Id is retrieved."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved company meta information.")
        ]
    )
    @GetMapping(
        value = ["/companies/{companyId}"],
        produces = ["application/json"]
    )

    /**
     * A method to retrieve company meta information for one specific company identified by its company Id
     * @param companyId identifier of the company in dataland
     * @return meta information (company Id and name)
     */
    fun getCompanyById(@PathVariable("companyId") companyId: String): ResponseEntity<CompanyMetaInformation>
}
