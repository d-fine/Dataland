package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandbackend.model.Company
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataIdentifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import javax.validation.Valid

/**
 * Defines the restful dataland-backend API regarding data exchange of company data.
 */

@RequestMapping("/")
interface CompanyAPI {
    @Operation(
        summary = "Show all stored companies.",
        description = "Retrieves a map of all existing companies with their company IDs as keys."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved map of companies.")
        ]
    )
    @GetMapping(
        value = ["/company"],
        produces = ["application/json"]
    )
    /**
     * Returns info (companyId and companyInfo) of all currently available companies in the data store.
     */
    fun getAllCompanies(): ResponseEntity<List<CompanyMetaInformation>>

    @Operation(
        summary = "Add a new company.",
        description = "The uploaded company info is added to the data store, the generated company id is returned."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added company to the data store.")
        ]
    )
    @PostMapping(
        value = ["/company"],
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    /**
     * A method to store company info via dataland into the data store
     * @param companyName info about the company to be stored
     * @return meta information about the stored company (id and company name)
     */
    fun postCompany(@Valid @RequestBody companyName: String): ResponseEntity<CompanyMetaInformation>

    @Operation(
        summary = "Retrieve specific companies from the data store.",
        description = "Companies identified via the provided company name are retrieved."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved a map of companies.")
        ]
    )
    @GetMapping(
        value = ["/company/{companyName}"],
        produces = ["application/json"]
    )
    /**
     * A method to retrieve specific companies identified by their company names
     * @param companyName identifier used to search for companies in the data store
     * @return all companies whose names match with the companyName provided as search input
     */
    fun getCompanyByName(@PathVariable("companyName") companyName: String): ResponseEntity<List<CompanyMetaInformation>>

    @Operation(
        summary = "Retrieve list of existing data sets for given company.",
        description = "Todo"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved list of data sets.")
        ]
    )
    @GetMapping(
        value = ["/company/{companyId}/data"],
        produces = ["application/json"]
    )
    /**
     * Todo
     * A method to retrieve specific companies identified by their company names
     * @param companyId identifier used to search for companies in the data store
     * @return all companies whose names match with the companyName provided as search input
     */
    fun getCompanyDataSets(@PathVariable("companyId") companyId: String): ResponseEntity<List<DataIdentifier>>
}
