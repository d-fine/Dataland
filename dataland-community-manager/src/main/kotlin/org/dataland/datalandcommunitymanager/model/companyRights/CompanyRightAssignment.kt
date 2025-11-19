package org.dataland.datalandcommunitymanager.model.companyRights

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.CommunityManagerOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandcommunitymanager.entities.CompanyRightEntity
import java.util.UUID

/**
 * DTO class for the CompanyRightEntity.
 * @param companyId of the company involved in the assignment
 * @param companyRight the right assigned to the company
 */
data class CompanyRightAssignment<IdType>(
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: IdType,
    @field:Schema(
        description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_RIGHT_DESCRIPTION,
    )
    val companyRight: CompanyRight,
) {
    /**
     * Convert this CompanyRightAssignment to the associated CompanyRightEntity.
     */
    fun toCompanyRightEntity(): CompanyRightEntity =
        CompanyRightEntity(
            companyId = UUID.fromString(companyId.toString()),
            companyRight = companyRight,
        )

    /**
     * Convert this CompanyRightAssignment to the associated CompanyRightId.
     */
    fun toCompanyRightId(): CompanyRightId =
        CompanyRightId(
            companyId = UUID.fromString(companyId.toString()),
            companyRight = companyRight,
        )

    /**
     * Convert this CompanyRightAssignment to one where companyId is of type UUID.
     */
    fun toCompanyRightAssignmentUUID(): CompanyRightAssignment<UUID> =
        CompanyRightAssignment(
            companyId = ValidationUtils.convertToUUID(companyId.toString()),
            companyRight = companyRight,
        )
}
