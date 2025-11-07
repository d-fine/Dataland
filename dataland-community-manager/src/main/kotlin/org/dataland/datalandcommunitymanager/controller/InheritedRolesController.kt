package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcommunitymanager.api.InheritedRolesApi
import org.dataland.datalandcommunitymanager.services.InheritedRolesManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the inherited roles endpoints.
 */
@RestController
class InheritedRolesController(
    @Autowired private val inheritedRolesManager: InheritedRolesManager,
) : InheritedRolesApi {
    override fun getInheritedRoles(userId: String): ResponseEntity<Map<String, List<String>>> =
        ResponseEntity.ok(
            inheritedRolesManager.getInheritedRoles(
                ValidationUtils.convertToUUID(userId),
            ),
        )
}
