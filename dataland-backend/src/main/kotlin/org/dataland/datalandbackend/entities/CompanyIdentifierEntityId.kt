package org.dataland.datalandbackend.entities

import jakarta.persistence.Embeddable
import java.io.Serializable
import org.dataland.datalandbackend.model.enums.company.IdentifierType

/** The Composite Id for the company identifier entity */
@Embeddable
data class CompanyIdentifierEntityId(
  var identifierValue: String,
  var identifierType: IdentifierType,
) : Serializable {
  companion object {
    private const val serialVersionUID: Long = 1
  }
}
