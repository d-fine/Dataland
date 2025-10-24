package org.dataland.datalandcommunitymanager.model.companyRights

import java.util.UUID

/**
 * ID class for the CompanyRightEntity.
 */
data class CompanyRightId(
    val companyId: UUID,
    val companyRight: CompanyRight,
)
