package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.UserUploadsApi
import org.dataland.datalandbackend.entities.DataMetaInformationForMyDatasets
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Defines the restful dataland-backend API regarding user data exchange
 */
@RestController
class UserUploadsController(
    @Autowired val dataMetaInformationManager: DataMetaInformationManager,
) : UserUploadsApi {
    override fun getUserUploadsDataMetaInformation(userId: String):
        ResponseEntity<List<DataMetaInformationForMyDatasets>> {
        if (DatalandAuthentication.fromContextOrNull()?.userId != userId) {
            throw InsufficientRightsApiException(
                "You are not allowed to retrieve information about this user.",
                "You are not allowed to retrieve information about this user.",
            )
        }
        return ResponseEntity.ok(dataMetaInformationManager.getUserDataMetaInformation(userId))
    }
}
