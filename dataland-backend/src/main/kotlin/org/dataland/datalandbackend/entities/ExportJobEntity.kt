package org.dataland.datalandbackend.entities

import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.springframework.core.io.InputStreamResource
import java.util.UUID

/**
 * Only to be stored in memory.
 */
class ExportJobEntity(
    val id: UUID,
    var fileToExport: InputStreamResource?,
    var progressState: ExportJobProgressState = ExportJobProgressState.Pending,
    var creationTime: Long,
)
