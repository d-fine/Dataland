package org.dataland.datalandbackend.entities

import org.dataland.datalandbackend.model.enums.company.IdentifierType
import java.io.Serializable
import javax.persistence.Embeddable

@Embeddable
data class CompanyIdentifierId(
    var identifierValue: String,
    var identifierType: IdentifierType,
) : Serializable