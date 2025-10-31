package org.dataland.datalandaccountingservice.model

import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

/**
 * Composite primary key class for BilledRequestEntity.
 */
@Embeddable
data class BilledRequestEntityId(
    var billedCompanyId: UUID,
    var dataSourcingId: UUID,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
