package org.dataland.datalandcommunitymanager.model.companyRights

import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

/**
 * ID class for CompanyRightEntity. Used instead of CompanyRightAssignment<UUID>, since
 * Hibernate does not support generic types in @IdClass.
 */
@Embeddable
data class CompanyRightId(
    val companyId: UUID,
    val companyRight: CompanyRight,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1
    }
}
