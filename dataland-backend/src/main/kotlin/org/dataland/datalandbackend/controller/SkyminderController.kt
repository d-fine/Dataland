package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.SkyminderAPI
import org.dataland.skyminderClient.model.ContactInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the API for using Skyminder
 * @param skyminderClient implementation of the DataConnectorInterface that defines how to connect to the data
 * source (e.g. Skyminder)
 */

@RestController
class SkyminderController(@Autowired var skyminderClient: SkyminderConnectorInterface) : SkyminderAPI {
    override fun getDataSkyminderRequest(countryCode: String, companyName: String):
        ResponseEntity<List<ContactInformation>> {
        return ResponseEntity.ok(
            this.skyminderClient.getContactInformation(
                countryCode = countryCode,
                name = companyName
            )
        )
    }
}
