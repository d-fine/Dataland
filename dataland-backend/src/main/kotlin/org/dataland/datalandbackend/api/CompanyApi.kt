package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.CompanyAvailableDistinctValues
import org.dataland.datalandbackend.model.CompanyIdAndName
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.CompanyInformationPatch
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

/**
 * Defines the restful dataland-backend API regarding company data.
 */
@RequestMapping("/companies")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface CompanyApi {

    /**
     * A method to create a new company entry in dataland
     * @param companyInformation includes the company information
     * @return information about the stored company, including the generated company ID
     */
    @Operation(
        summary = "Add a new company.",
        description = "A new company is added using the provided information, the generated company ID is returned.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added company."),
        ],
    )
    @PostMapping(
        produces = ["application/json"],
        consumes = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun postCompany(
        @Valid @RequestBody
        companyInformation: CompanyInformation,
    ):
        ResponseEntity<StoredCompany>

    /**
     * A method to retrieve specific companies with framework data identified by different filters
     * If the filters are not set, all companies in the data store are returned.
     * @param searchString string used for substring matching
     * @param dataTypes this function only returns companies that have data for the specified dataTypes.
     * if none is specified, it is filtered all data types are allowed
     * @param countryCodes If set & non-empty,
     * this function only returns companies that have a country code contained in the set
     * @param sectors If set & non-empty, this function only returns companies that belong to a sector in the set
     * @param onlyCompanyNames boolean determining if the search should be solely against the companyNames
     * @param onlyWithDataFromCurrentUser boolean determining if the search should only find companies with datasets
     * uploaded by the current user
     * @return information about all companies with framework data matching the search criteria
     */
    @Operation(
        summary = "Retrieve specific companies with framework data by different filters" +
            " or just all companies from the data store.",
        description = "Companies with associated framework data identified via the provided company name/identifier" +
            " are retrieved and filtered by countryCode, sector and available framework data." +
            " Empty/Unspecified filters are ignored.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved companies."),
        ],
    )
    @GetMapping(
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getCompanies(
        @RequestParam searchString: String? = null,
        @RequestParam dataTypes: Set<DataType>? = null,
        @RequestParam countryCodes: Set<String>? = null,
        @RequestParam sectors: Set<String>? = null,
        @RequestParam onlyCompanyNames: Boolean = false,
        @RequestParam onlyWithDataFromCurrentUser: Boolean = false,
    ):
        ResponseEntity<List<StoredCompany>>

    /**
     * A method to retrieve companies with names or identifiers matching a search string
     * @param searchString string used for substring matching in the name and the identifiers of a company
     * @return names of the first 100 companies matching the search criteria
     */
    @Operation(
        summary = "Retrieve specific companies by searching their names and identifiers",
        description = "Companies identified via the provided company name/identifier are retrieved",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved company names."),
        ],
    )
    @GetMapping(
        value = ["/names"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getCompaniesBySearchString(
        @RequestParam searchString: String,
    ):
        ResponseEntity<List<CompanyIdAndName>>

    /**
     * A method to check if an identifier of a given type exists
     * @param identifierType the type of the identifier
     * @param identifier the identifier
     */
    @Operation(
        summary = "Checks that an identifier of specified type exists.",
        description = "Checks that an identifier of specified type exists.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully checked that identifier exists."),
            ApiResponse(responseCode = "404", description = "Successfully checked that identifier does not exist."),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/identifiers/{identifierType}/{identifier}"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun existsIdentifier(
        @PathVariable("identifierType") identifierType: IdentifierType,
        @PathVariable("identifier") identifier: String,
    )

    /**
     * A method used to retrieve all available distinct values for framework type, country code & sector
     * to be used by the search UI
     */
    @Operation(
        summary = "Retrieve available distinct values for company search filters",
        description = "Distinct values for the parameter countryCode and sector are returned",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved values."),
        ],
    )
    @GetMapping(
        value = ["/meta-information"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getAvailableCompanySearchFilters(): ResponseEntity<CompanyAvailableDistinctValues>

    /**
     * A method to retrieve company information for one specific company identified by its company Id
     * @param companyId identifier of the company in dataland
     * @return information about the company
     */
    @Operation(
        summary = "Retrieve company information.",
        description = "Company information behind the given company Id is retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved company information."),
        ],
    )
    @GetMapping(
        value = ["/{companyId}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER') or @CompanyQueryManager.isCompanyPublic(#companyId)")
    fun getCompanyById(@PathVariable("companyId") companyId: String): ResponseEntity<StoredCompany>

    /**
     * A method to update company informtion for one specific company identified by its company Id
     * @param companyId identifier of the company in dataland
     * @param companyInformation includes the company information
     * @return updated information about the company
     */
    @Operation(
        summary = "Update company information selectively",
        description = "Changed elements of a company information behind the given company Id is updated.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully updated company information."),
        ],
    )
    @PatchMapping(
        value = ["/{companyId}"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun patchCompanyById(
        @PathVariable("companyId") companyId: String,
        @Valid @RequestBody
        companyInformationPatch: CompanyInformationPatch,
    ): ResponseEntity<StoredCompany>

    /**
     * A method to update company informtion entirely
     * @param companyId identifier of the company in dataland
     * @param companyInformation includes the company information
     * @return updated information about the company
     */
    @Operation(
        summary = "Update company information entirely",
        description = "all elements of a company information behind the given company Id is updated.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully updated company information."),
        ],
    )
    @PutMapping(
        value = ["/{companyId}"],
        consumes = ["application/json"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_UPLOADER')")
    fun putCompanyById(
        @PathVariable("companyId") companyId: String,
        @Valid @RequestBody
        companyInformation: CompanyInformation,
    ): ResponseEntity<StoredCompany>

    /**
     * A method to get the teaser company IDs.
     * @return a list of all company IDs currently set as teaser companies
     */
    @Operation(
        summary = "Get the company IDs of the teaser companies.",
        description = "A list of all company IDs that are currently set as teaser companies (accessible without " +
            "authentication).",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully returned teaser companies."),
        ],
    )
    @GetMapping(
        value = ["/teaser"],
        produces = ["application/json"],
    )
    fun getTeaserCompanies(): List<String>
}
