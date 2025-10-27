package org.dataland.datalandcommunitymanager.model.companyRights

import java.util.UUID

/**
 * ID class for CompanyRightEntity.
 */
data class CompanyRightId(
    val companyId: UUID,
    val companyRight: CompanyRight,
)
