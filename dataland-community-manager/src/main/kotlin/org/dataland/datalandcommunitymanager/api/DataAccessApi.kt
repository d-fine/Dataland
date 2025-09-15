package org.dataland.datalandcommunitymanager.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import java.util.UUID

/**
 * Defines restful Dataland Community Manager API regarding data access.
 */
@RequestMapping("/requests")
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
interface DataAccessApi {
    /**
     * A method to check if the logged-in user can access a specific dataset.
     * The dataset is specified by a companyId, dataType and a reportingPeriod.
     * @param companyId of the company for which the user might have the role
     * @param dataType of the corresponding framework
     * @param reportingPeriod of the dataset
     */
    @Operation(
        summary = "This head request checks whether the logged-in user has access to dataset.",
        description = "This head request checks whether the logged-in user has access to dataset.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "The user can access the dataset."),
            ApiResponse(
                responseCode = "404",
                description = "Either the specified dataset does not exist or the user cannot access the dataset.",
            ),
        ],
    )
    @RequestMapping(
        method = [RequestMethod.HEAD],
        value = ["/dataset-access/{companyId}/{dataType}/{reportingPeriod}/{userId}"],
    )
    fun hasAccessToDataset(
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
        )
        @PathVariable("companyId") companyId: UUID,
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_EXAMPLE,
        )
        @PathVariable("dataType") dataType: String,
        @Parameter(
            description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
            example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
        )
        @PathVariable("reportingPeriod") reportingPeriod: String,
        @Parameter(
            description = CommunityManagerOpenApiDescriptionsAndExamples.DATA_REQUEST_USER_ID_DESCRIPTION,
            example = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
        )
        @PathVariable("userId") userId: UUID,
    )
}
