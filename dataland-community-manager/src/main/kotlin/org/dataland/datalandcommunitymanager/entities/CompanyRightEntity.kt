package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRightId
import java.util.UUID

/**
 * Entity class for company ID to company right pairs.
 */
@Entity
@Table(name = "company_rights")
@IdClass(CompanyRightId::class)
class CompanyRightEntity(
    @Id
    val companyId: UUID,
    @Id
    @Enumerated(EnumType.STRING)
    val companyRight: CompanyRight,
)
