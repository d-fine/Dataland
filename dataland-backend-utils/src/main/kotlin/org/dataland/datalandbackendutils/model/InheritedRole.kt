package org.dataland.datalandbackendutils.model

/**
 * Roles which a user who has a CompanyRole for a specific company inherits specifically for that company.
 * These roles lead to certain rights, which may be company-specific or global on Dataland.
 */
enum class InheritedRole {
    DatalandMember,
}
