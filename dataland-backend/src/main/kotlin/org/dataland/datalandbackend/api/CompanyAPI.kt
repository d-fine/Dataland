package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.enums.StockIndex
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
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
@SecurityRequirement(name = "default-auth")
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
        produces = ["application/json"],
        consumes = ["application/json"]
    )
    @PreAuthorize("hasRole(@RoleContainer.DATA_UPLOADER)")
    /**
     * A method to create a new company entry in dataland
     * @param companyInformation includes the company information
     * @return information about the stored company, including the generated company ID
     */
    fun postCompany(@Valid @RequestBody companyInformation: CompanyInformation):
        ResponseEntity<StoredCompany>

    @Operation(
        summary = "Retrieve specific companies by name/identifier/index or just all companies from the data store.",
        description = "Companies identified via the provided company name/identifier/index are retrieved. " +
            "If only an empty string is passed as search argument, all companies in the data store are returned." +
            "If selectedIndex is not null, all companies in Dataland associated to the given stock index are returned."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved companies.")
        ]
    )
    @GetMapping(
        produces = ["application/json"]
    )
    @PreAuthorize("hasRole(@RoleContainer.DATA_READER)")
    /**
     * A method to retrieve specific companies identified by their company names identifier or stock index
     * If only an empty string is passed as search argument, all companies in the data store are returned.
     * If selectedIndex is not null, all companies in Dataland associated to the given stock index are returned.
     * @param searchString string used for substring matching
     * @param selectedIndex StockIndex Enum used to filter against stock indices
     * @param onlyCompanyNames boolean determining if the search should be solely against the companyNames
     * @return information about all companies matching the search criteria
     */
    fun getCompanies(
        @RequestParam searchString: String? = null,
        @RequestParam selectedIndex: StockIndex? = null,
        @RequestParam onlyCompanyNames: Boolean = false
    ):
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
    @PreAuthorize("hasRole(@RoleContainer.DATA_READER) or @PreviewStuff.isCompanyPublic(#companyId)")
    /**
     * A method to retrieve company information for one specific company identified by its company Id
     * @param companyId identifier of the company in dataland
     * @return information about the company
     */
    fun getCompanyById(@PathVariable("companyId") companyId: String): ResponseEntity<StoredCompany>
}
