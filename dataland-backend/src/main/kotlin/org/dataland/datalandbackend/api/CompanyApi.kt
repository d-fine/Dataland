package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.dataland.datalandbackend.interfaces.CompanyIdAndName
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.companies.AggregatedFrameworkDataSummary
import org.dataland.datalandbackend.model.companies.CompanyAvailableDistinctValues
import org.dataland.datalandbackend.model.companies.CompanyDataOwners
import org.dataland.datalandbackend.model.companies.CompanyId
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.companies.CompanyInformationPatch
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

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
     * A method to create a new data ownership relation in dataland
     * @param companyId the ID of the company to which a new data owner is to be added
     * @param userId the ID of the user who is to be added as company data owner
     * @return information about the stored company data ownership relation
     */
    @Operation(
        summary = "Add a new data owner to a company.",
        description = "A new data owner is added to the existing list for the specified company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully added data owner."),
        ],
    )
    @PostMapping(
        produces = ["application/json"],
        value = ["/{companyId}/data-owners/{userId}"],

    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun postDataOwner(
        @PathVariable("companyId") companyId: UUID,
        @PathVariable("userId") userId: UUID,
    ):
        ResponseEntity<CompanyDataOwners>

    /**
     * A method to retrieve a  data owner information from companies in dataland
     * @param companyId the ID of the company to which a new data owner is to be added
     * @return userId of the data owner(s) of a specified company
     */
    @Operation(
        summary = "Retrieve data owner(s) of a company.",
        description = "Get a list of data owner(s) for the specified company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved data owner."),
            ApiResponse(responseCode = "404", description = "The specified company does not exist on Dataland."),
        ],
    )
    @GetMapping(
        produces = ["application/json"],
        value = ["/{companyId}/data-owners"],

    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDataOwners(
        @PathVariable("companyId") companyId: UUID,
    ):
        ResponseEntity<List<String>>

    /**
     * A method to delete a data ownership relation in dataland
     * @param companyId the ID of the company to which the data ownership should be removed from
     * @param userId the ID of the user who is to be removed as company data owner
     * @return information about the deleted company data ownership relation
     */
    @Operation(
        summary = "Delete a data owner from a specified company.",
        description = "An existing data owner is deleted from the existing list for the specified company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully deleted data owner."),
            ApiResponse(responseCode = "404", description = "The specified company doesn't exist on Dataland."),
            ApiResponse(responseCode = "404", description = "Data owner doesn't exist for the specified company."),
        ],
    )
    @DeleteMapping(
        produces = ["application/json"],
        value = ["/{companyId}/data-owners/{userId}"],

    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteDataOwner(
        @PathVariable("companyId") companyId: UUID,
        @PathVariable("userId") userId: UUID,
    ):
        ResponseEntity<CompanyDataOwners>

    /**
     * A method to check if a user specified via their ID is data owner for a certain company
     * @param companyId the ID of the company
     * @param userId the ID of the user
     */
    @Operation(
        summary = "Validation of a user-company combination with regards to data ownership.",
        description = "Checks whether a user is data owner of a company.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "The specified user is data owner of the company."),
            ApiResponse(responseCode = "404", description = "The specified company does not exist on Dataland."),
            ApiResponse(responseCode = "404", description = "The specified user isn't data owner of the company."),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/{companyId}/{userId}"],
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun isUserDataOwnerForCompany(
        @PathVariable("companyId") companyId: UUID,
        @PathVariable("userId") userId: UUID,
    )
}
