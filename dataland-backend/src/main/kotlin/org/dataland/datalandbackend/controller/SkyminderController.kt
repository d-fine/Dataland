package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.SkyminderAPI
import org.dataland.skyminderClient.interfaces.DataConnectorInterface
import org.dataland.skyminderClient.model.ContactInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the API for using Skyminder
 * @param dataConnector implementation of the DataConnectorInterface that defines how to connect to the data
 * source (e.g. Skyminder)
 */

@RestController
class SkyminderController(@Autowired var dataConnector: DataConnectorInterface) : SkyminderAPI {
    override fun getDataSkyminderRequest(countryCode: String, name: String): ResponseEntity<List<ContactInformation>> {
        return ResponseEntity.ok(this.dataConnector.getContactInformation(countryCode = countryCode, name = name))
    }
}
