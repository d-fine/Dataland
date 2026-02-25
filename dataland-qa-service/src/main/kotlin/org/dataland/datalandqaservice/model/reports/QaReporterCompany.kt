package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import jakarta.persistence.Embeddable
import java.util.UUID

/**
 * Hallo
 */
@Embeddable
data class QaReporterCompany(
    val reportUserId: UUID,
    val reportCompanyName: String,
    val reportCompanyId: UUID,
)
