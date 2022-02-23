package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.ContactInformation
import org.springframework.stereotype.Component

@Component
interface DataConnectorInterface {
    fun getContactInformation(countryCode: String, name: String): List<ContactInformation>
}