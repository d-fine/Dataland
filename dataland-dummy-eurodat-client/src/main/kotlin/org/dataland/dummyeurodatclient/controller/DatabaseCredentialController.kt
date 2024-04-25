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
class DatabaseCredentialController(@Value("\${dataland.eurodatdummyclient.jdbc-url}")
                                   private val eurodatJdbcUrl: String,
                                   @Value("\${dataland.eurodatdummyclient.jdbc-username}")
                                   private val eurodatUsername: String,
                                   @Value("\${dataland.eurodatdummyclient.jdbc-password}")
                                   private val eurodatPassword: String,
    ) : DatabaseCredentialResourceApi {
    val credentials = Credentials(
        eurodatJdbcUrl,
        eurodatUsername,
        eurodatPassword
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
