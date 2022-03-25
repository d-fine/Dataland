package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.PostCompanyRequestBody
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

@RequestMapping("/companies")
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
        value = [""],
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    /**
     * A method to create a new company entry in dataland
     * @param postCompanyRequestBody includes the company name of the company to be created
     * @return meta information about the stored company (id and company name)
     */
    fun postCompany(@Valid @RequestBody postCompanyRequestBody: PostCompanyRequestBody):
        ResponseEntity<CompanyMetaInformation>

    @Operation(
        summary = "Retrieve specific companies by name or just all companies from the data store.",
        description = "Companies identified via the provided company name are retrieved. " +
            "If company name is an empty string, all companies in the data store are returned."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved companies.")
        ]
    )
    @GetMapping(
        value = [""],
        produces = ["application/json"]
    )
    /**
     * A method to retrieve specific companies identified by their company names
     * If an empty string is passed as company name, all companies in the data store will be returned.
     * @param companyName identifier used to search for companies in the data store (can also be an empty string)
     * @return all companies matching the search criteria
     */
    fun getCompaniesByName(@RequestParam companyName: String? = null):
        ResponseEntity<List<CompanyMetaInformation>>
/*

This endpoint will be obsolete as soon as AllDataAPI is implemented.

    @Operation(
        summary = "Retrieve list of meta info about data sets for company.",
        description = "A List of meta info about all data sets for of the given company is retrieved."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved list of data sets.")
        ]
    )
    @GetMapping(
        value = ["/{companyId}/data"],
        produces = ["application/json"]
    )
    /**
     * A method to retrieve meta info about all existing data sets of a specific company identified by the company ID
     * @param companyId identifier of the company
     * @return list of data meta info about all existing data sets of the specified company
     */
    fun getCompanyDataSets(@PathVariable("companyId") companyId: String): ResponseEntity<List<DataMetaInformation>>
*/

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
        value = ["/{companyId}"],
        produces = ["application/json"]
    )

    /**
     * A method to retrieve company meta information for one specific company identified by its company Id
     * @param companyId identifier of the company in dataland
     * @return meta information (company Id and name)
     */
    fun getCompanyById(@PathVariable("companyId") companyId: String): ResponseEntity<CompanyMetaInformation>
}
