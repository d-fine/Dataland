package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.interfaces.CompanyIdAndName
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.companies.AggregatedFrameworkDataSummary
import org.dataland.datalandbackend.model.companies.CompanyAvailableDistinctValues
import org.dataland.datalandbackend.model.companies.CompanyId
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.companies.CompanyInformationPatch
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postCompany(
        @Valid @RequestBody
        companyInformation: CompanyInformation,
    ):
        ResponseEntity<StoredCompany>

    /**
     * A method to retrieve just the basic information about specific companies
     * If the filters are not set, all companies in the data store are returned.
     * @param searchString string used for substring matching
     * @param dataTypes this function only returns companies that have data for a specified dataType.
     * @param countryCodes If set & non-empty,
     * this function only returns companies that have a country code contained in the set
     * @param sectors If set & non-empty, this function only returns companies that belong to a sector in the set
     * uploaded by the current user
     * @return basic information about all companies with approved framework data matching the search criteria
     */
    @Operation(
        summary = "Retrieve just the basic information about specific companies.",
        description = "The basic information about companies" +
            " via the provided company name/identifier" +
            " are retrieved and filtered by countryCode, sector and available framework data." +
            " Empty/Unspecified filters are ignored.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved basic company information."),
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
        @RequestParam chunkSize: Int? = null,
        @RequestParam chunkIndex: Int? = null,
    ):
        ResponseEntity<List<BasicCompanyInformation>>

    /**
     * A method to retrieve just the number of companies identified by different filters
     * If the filters are not set, all companies in the data store are returned
     * @param searchString string used for substring matching
     * @param dataTypes If set & non-empty, this function only counts companies that have data for a dataType in the set
     * @param countryCodes If set & non-empty,
     * this function only counts companies that have a country code contained in the set
     * @param sectors If set & non-empty, this function only counts companies that belong to a sector in the set
     * uploaded by the current user
     * @return the number of companies matching the search criteria
     */
    @Operation(
        summary = "Retrieve the number of companies" +
            " satisfying different filters.",
        description = "The number of companies" +
            " via the provided company name/identifier" +
            " are retrieved and filtered by countryCode, sector and available framework data." +
            " Empty/Unspecified filters are ignored.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved number of companies."),
        ],
    )
    @GetMapping(
        value = ["/numberOfCompanies"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getNumberOfCompanies(
        @RequestParam searchString: String? = null,
        @RequestParam dataTypes: Set<DataType>? = null,
        @RequestParam countryCodes: Set<String>? = null,
        @RequestParam sectors: Set<String>? = null,
    ): ResponseEntity<Int>

    /**
     * A method to retrieve companies with names or identifiers matching a search string
     * @param searchString string used for substring matching in the name and the identifiers of a company
     * @param resultLimit number of search results to be retrieved
     * @return names of the first [resultLimit] companies matching the search criteria
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
    fun getCompaniesBySearchString(
        @RequestParam searchString: String,
        @RequestParam(defaultValue = "100") resultLimit: Int,
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
     * A method to get the company an identifier of a given type exists
     * @param identifierType the type of the identifier
     * @param identifier the identifier
     */
    @Operation(
        summary = "Gets the company ID for an identifier of specified type.",
        description = "Get the company ID for an identifier of specified type.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Found a company corresponding the identifier."),
            ApiResponse(responseCode = "404", description = "Found no company corresponding the identifier."),
        ],
    )
    @GetMapping(
        value = ["/identifiers/{identifierType}/{identifier}"],
        produces = ["application/json"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun getCompanyIdByIdentifier(
        @PathVariable("identifierType") identifierType: IdentifierType,
        @PathVariable("identifier") identifier: String,
    ): ResponseEntity<CompanyId>

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
     * @param companyInformationPatch includes the company information
     * @return updated information about the company
     */
    @Operation(
        summary = "Update company information selectively",
        description = "Provided fields of the company associated with the given company Id are updated.",
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
    @PreAuthorize(
        "hasRole('ROLE_USER') and " +
            "@CompanyRoleChecker.canUserPatchFieldsForCompany(#companyInformationPatch, #companyId)",
    )
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
        description = "Replace all company information of the company associated with the given company Id",
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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

    /**
     * A method used to retrieve the aggregated data summary for all frameworks
     * @param companyId the identifier of the company to collect the information for
     * @returns the collected aggregated data summary per framework
     */
    @Operation(
        summary = "Retrieve aggregated data summary for all frameworks",
        description = "For each framework retrieves the amount of available reporting periods",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved values."),
        ],
    )
    @GetMapping(
        value = ["/{companyId}/aggregated-framework-data-summary"],
        produces = ["application/json"],
    )
    fun getAggregatedFrameworkDataSummary(
        @PathVariable("companyId") companyId: String,
    ): ResponseEntity<Map<DataType, AggregatedFrameworkDataSummary>>

    /**
     * A method to retrieve company information for one specific company identified by its company ID
     * @param companyId identifier of the company in dataland
     * @return information about the company without framework information
     */
    @Operation(
        summary = "Retrieve company information.",
        description = "Company information behind the given company ID is retrieved.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved company information."),
        ],
    )
    @GetMapping(
        value = ["/{companyId}/info"],
        produces = ["application/json"],
    )
    fun getCompanyInfo(@PathVariable("companyId") companyId: String): ResponseEntity<CompanyInformation>

    /**
     * A method to check if a companyId is valid
     * @param companyId the identifier
     */
    @Operation(
        summary = "Checks that a companyId exists.",
        description = "Checks that a companyId exists.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully checked that companyId exists."),
            ApiResponse(responseCode = "404", description = "Successfully checked that companyIdr does not exist."),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/{companyId}"],
    )
    @PreAuthorize("hasRole('ROLE_USER')")
    fun isCompanyIdValid(
        @PathVariable("companyId") companyId: String,
    )
}
