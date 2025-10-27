package org.dataland.datalandcommunitymanager.model.companyRights

import org.dataland.datalandcommunitymanager.entities.CompanyRightEntity
import java.util.UUID

/**
 * DTO class for the CompanyRightEntity.
 */
data class CompanyRightAssignment<IdType>(
    val companyId: IdType,
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
}
