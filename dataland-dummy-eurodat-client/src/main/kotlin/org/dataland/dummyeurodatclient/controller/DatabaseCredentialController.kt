package org.dataland.dummyeurodatclient.controller

import org.dataland.dummyeurodatclient.openApiServer.api.DatabaseCredentialResourceApi
import org.dataland.dummyeurodatclient.openApiServer.model.Credentials
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the database credential api
 */
@RestController
class DatabaseCredentialController : DatabaseCredentialResourceApi {
    val credentials = Credentials(
        "jdbc:postgresql://dummy-eurodat-db:5432/eurodat_db_name", //TODO get from app props
        "eurodat_db_user", //TODO get from app props
        "eurodat_db_password" //TODO get from app props
    )

    override fun apiV1ClientControllerCredentialServiceDatabaseSafedepositAppIdGet(appId: String): ResponseEntity<Credentials> {
        return ResponseEntity.ok(credentials)
    }

    override fun apiV1ClientControllerCredentialServiceDatabaseExternalTransactionIdGet(transactionId: String): ResponseEntity<Credentials> {
        return ResponseEntity.ok(credentials)
    }
}
