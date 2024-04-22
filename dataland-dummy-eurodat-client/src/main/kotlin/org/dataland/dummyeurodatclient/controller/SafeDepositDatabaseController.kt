package org.dataland.dummyeurodatclient.controller

import org.dataland.dummyeurodatclient.openApiServer.api.SafeDepositDatabaseResourceApi
import org.dataland.dummyeurodatclient.openApiServer.model.SafeDepositDatabaseRequest
import org.dataland.dummyeurodatclient.openApiServer.model.SafeDepositDatabaseResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the safe deposit box api
 */
@RestController
class SafeDepositDatabaseController : SafeDepositDatabaseResourceApi {
    override fun apiV1ClientControllerDatabaseServicePost(safeDepositDatabaseRequest: SafeDepositDatabaseRequest?):
            ResponseEntity<SafeDepositDatabaseResponse> {
        val response = SafeDepositDatabaseResponse("Database already exists")
        return ResponseEntity.ok(response)
    }

    override fun apiV1ClientControllerDatabaseServiceAppIdDelete(appId: String):
            ResponseEntity<SafeDepositDatabaseResponse> {
        return ResponseEntity.ok(SafeDepositDatabaseResponse("dummy"))
    }
}
