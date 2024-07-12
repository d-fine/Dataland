package org.dataland.datalandbackend.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Defines the restful dataland-backend API regarding deletion endpoint for admin users
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
@RequestMapping("/data/{dataId}")
fun interface DataDeletionApi {

    /**
     * A method to delete data via Dataland into a data store
     * @param dataId consisting of the ID of the dataset to be removed
     * @return meta info about the stored data including the ID of the created entry in the data store
     */
    @Operation(
        summary = "Delete a data set.",
        description = "The data is removed from the data store.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully deleted the dataset."),
        ],
    )
    @DeleteMapping()
    @PreAuthorize(
        "hasRole('ROLE_ADMIN') or " +
            "(hasRole('ROLE_USER') and " +
            "(@CompanyRoleChecker.hasCurrentUserGivenRoleForCompanyOfDataId(" +
            "#dataId, T(org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole).CompanyOwner" +
            ") or " +
            "@CompanyRoleChecker.hasCurrentUserGivenRoleForCompanyOfDataId(" +
            "#dataId, T(org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole).DataUploader" +
            "))" +
            ")",
    )
    fun deleteCompanyAssociatedData(
        @PathVariable("dataId")
        @Valid
        @RequestBody
        dataId: String,
    )
}
