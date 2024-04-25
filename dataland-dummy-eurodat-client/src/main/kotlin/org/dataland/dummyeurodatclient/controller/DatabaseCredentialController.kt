package org.dataland.dummyeurodatclient.controller

import org.dataland.dummyeurodatclient.openApiServer.api.DatabaseCredentialResourceApi
import org.dataland.dummyeurodatclient.openApiServer.model.Credentials
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the database credential api
 */
@RestController
class DatabaseCredentialController(
    @Value("\${dummy.eurodat.db.postgres-db}")
    private val postgresDb: String,
    @Value("\${dummy.eurodat.db.postgres-user}")
    private val postgresUser: String,
    @Value("\${dummy.eurodat.db.postgres-password}")
    private val postgresPassword: String,
) : DatabaseCredentialResourceApi {
    val credentials = Credentials(
        "jdbc:postgresql://dummy-eurodat-db:5432/$postgresDb",
        postgresUser,
        postgresPassword
    )

    override fun apiV1ClientControllerCredentialServiceDatabaseSafedepositAppIdGet(appId: String):
            ResponseEntity<Credentials> {
        return ResponseEntity.ok(credentials)
    }

    override fun apiV1ClientControllerCredentialServiceDatabaseExternalTransactionIdGet(transactionId: String):
            ResponseEntity<Credentials> {
        return ResponseEntity.ok(credentials)
    }
}
