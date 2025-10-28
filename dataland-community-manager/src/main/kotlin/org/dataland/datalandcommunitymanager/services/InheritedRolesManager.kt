package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.utils.InheritedRolesUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service class for handling inherited roles.
 */
@Service("InheritedRolesManager")
class InheritedRolesManager
    @Autowired
    constructor(
        private val companyRolesManager: CompanyRolesManager,
        private val companyRightsManager: CompanyRightsManager,
    ) {
        /**
         * For the specified user, get a map from the Dataland IDs of companies in which this user has at least one
         * CompanyRole to the corresponding (company-specific) lists of inherited roles of the user.
         * @param userId the Dataland ID of the user in question
         * @return a map from company IDs to the lists of associated inherited roles
         */
        fun getInheritedRoles(userId: UUID): Map<String, List<String>> {
            val inheritedRolesMap = mutableMapOf<String, List<String>>()

            val associatedCompanyIdsOfUser =
                companyRolesManager
                    .getCompanyRoleAssignmentsByParameters(
                        companyRole = null,
                        companyId = null,
                        userId = userId.toString(),
                    ).map { it.companyId }
                    .toSet()

            associatedCompanyIdsOfUser.forEach { associatedCompanyId ->
                val associatedCompanyRights = companyRightsManager.getCompanyRights(UUID.fromString(associatedCompanyId))

                inheritedRolesMap[associatedCompanyId] =
                    InheritedRolesUtils
                        .getInheritedRoles(
                            associatedCompanyRights,
                        ).map { it.name }
            }

            return inheritedRolesMap
        }
    }
