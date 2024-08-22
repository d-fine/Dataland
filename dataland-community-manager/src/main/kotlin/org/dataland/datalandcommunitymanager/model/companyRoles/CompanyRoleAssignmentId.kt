package org.dataland.datalandcommunitymanager.model.companyRoles

import jakarta.persistence.Embeddable
import java.io.Serializable

/**
 * The primary key in the company role assignments table is a composite key. In order to define that composite key, this
 * class here is required.
 */
@Embeddable
data class CompanyRoleAssignmentId(
    var companyRole: CompanyRole,
    var companyId: String,
    var userId: String,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1
    }
}
