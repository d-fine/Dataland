package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.CompanyAvailableDistinctValues
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland-backend API regarding company data.
 */
@RequestMapping("/companies")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface CompanyApi {

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
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    /**
     * A method to create a new company entry in dataland
     * @param companyInformation includes the company information
     * @return information about the stored company, including the generated company ID
     */
    fun postCompany(@Valid @RequestBody companyInformation: CompanyInformation):
        ResponseEntity<StoredCompany>

    @Operation(
        summary = "Retrieve specific companies by different filters or just all companies from the data store.",
        description = "Companies identified via the provided company name/identifier are retrieved and filtered by" +
            "countryCode, sector and available framework data. Empty/Unspecified filters are ignored."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved companies.")
        ]
    )
    @GetMapping(
        produces = ["application/json"]
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    /**
     * A method to retrieve specific companies identified by different filters
     * If the filters are not set, all companies in the data store are returned.
     * @param searchString string used for substring matching
     * @param onlyCompanyNames boolean determining if the search should be solely against the companyNames
     * @param dataTypes If set & non-empty,
     * this function only returns companies that have data for the specified dataTypes
     * @param countryCodes If set & non-empty,
     * this function only returns companies that have a country code contained in the set
     * @param sectors If set & non-empty, this function only returns companies that belong to a sector in the set
     * @return information about all companies matching the search criteria
     */
    fun getCompanies(
        @RequestParam searchString: String? = null,
        @RequestParam dataTypes: Set<DataType>? = null,
        @RequestParam countryCodes: Set<String>? = null,
        @RequestParam sectors: Set<String>? = null,
        @RequestParam onlyCompanyNames: Boolean = false
    ):
        ResponseEntity<List<StoredCompany>>

    @Operation(
        summary = "Retrieve available distinct values for company search filters",
        description = "Distinct values for the parameter countryCode and sector are returned"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved values.")
        ]
    )
    @GetMapping(
        value = ["/meta-information"],
        produces = ["application/json"]
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    /**
     * A method used to retrieve all available distinct values for framework type, country code & sector
     * to be used by the search UI
     */
    fun getAvailableCompanySearchFilters(): ResponseEntity<CompanyAvailableDistinctValues>

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

    @PreAuthorize("hasRole('ROLE_USER') or @CompanyManager.isCompanyPublic(#companyId)")
    /**
     * A method to retrieve company information for one specific company identified by its company Id
     * @param companyId identifier of the company in dataland
     * @return information about the company
     */
    fun getCompanyById(@PathVariable("companyId") companyId: String): ResponseEntity<StoredCompany>

    @Operation(
        summary = "Get the company IDs of the teaser companies.",
        description = "A list of all company IDs that are currently set as teaser companies (accessible without " +
            "authentication)."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully returned teaser companies.")
        ]
    )
    @GetMapping(
        value = ["/teaser"],
        produces = ["application/json"]
    )

    /**
     * A method to get the teaser company IDs.
     * @return a list of all company IDs currently set as teaser companies
     */
    fun getTeaserCompanies(): List<String>
}
