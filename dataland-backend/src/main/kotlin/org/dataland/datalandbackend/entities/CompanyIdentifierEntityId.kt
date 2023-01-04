package org.dataland.datalandbackend.entities

import org.dataland.datalandbackend.model.enums.company.IdentifierType
import java.io.Serializable
import jakarta.persistence.Embeddable

/**
 * The Composite Id for the company identifier entity
 */
@Embeddable
data class CompanyIdentifierEntityId(
    var identifierValue: String,
    var identifierType: IdentifierType,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1
    }
}
