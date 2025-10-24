package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRightAssignment
import java.util.UUID

/**
 * Entity class for company ID to company right pairs.
 */
@Entity
@Table(name = "company_rights")
@IdClass(CompanyRightAssignment::class)
class CompanyRightEntity(
    @Id
    val companyId: UUID,
    @Id
    val companyRight: CompanyRight,
) {
    /**
     * Convert this CompanyRightEntity to the associated CompanyRightAssignment model object.
     */
    fun toCompanyRightAssignment(): CompanyRightAssignment =
        CompanyRightAssignment(
            companyId = companyId,
            companyRight = companyRight,
        )
}
