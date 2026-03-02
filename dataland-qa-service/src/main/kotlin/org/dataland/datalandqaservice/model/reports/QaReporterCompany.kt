package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import jakarta.persistence.Embeddable
import java.util.UUID

/**
 * Embeddable representing a company that submitted QA reports for a dataset under review.
 */
@Embeddable
data class QaReporterCompany(
    val reportCompanyName: String,
    val reportCompanyId: UUID?,
)
