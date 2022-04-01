package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.StoredCompany
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
        description = "A new company is added using the provided information, the response includes " +
                "the generated company ID."
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
     * @param companyInformation includes the company information
     * @return information about the stored company, including the generated company ID
     */
    fun postCompany(@Valid @RequestBody companyInformation: CompanyInformation):
        ResponseEntity<StoredCompany>

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
     * @return infrotmation about all companies matching the search criteria
     */
    fun getCompaniesByName(@RequestParam companyName: String? = null):
        ResponseEntity<List<StoredCompany>>

    @Operation(
        summary = "Retrieve company information.",
        description = "Company information behind the given company Id is retrieved."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved company information.")
        ]
    )
    @GetMapping(
        value = ["/{companyId}"],
        produces = ["application/json"]
    )

    /**
     * A method to retrieve company information for one specific company identified by its company Id
     * @param companyId identifier of the company in dataland
     * @return information about the company
     */
    fun getCompanyById(@PathVariable("companyId") companyId: String): ResponseEntity<StoredCompany>
}
