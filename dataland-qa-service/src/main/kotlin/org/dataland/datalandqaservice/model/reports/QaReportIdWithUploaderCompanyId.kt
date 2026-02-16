package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import jakarta.persistence.Embeddable
import java.util.UUID

/**
 * Data class to save uploader company at point of creation.
 */
@Embeddable
data class QaReportIdWithUploaderCompanyId(
    val qaReportId: UUID,
    val uploaderCompanyId: UUID?,
)
