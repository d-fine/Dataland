package org.dataland.datalandcommunitymanager.model.companyRoles

import io.swagger.v3.oas.annotations.media.Schema

/**
 * --- Generic API model ---
 * Contains all company roles that are currently supported in Dataland
 */
@Schema(
    enumAsRef = true,
)
enum class CompanyRole {
    CompanyOwner,
    DataUploader,
    MemberAdmin,
    Member,
}
