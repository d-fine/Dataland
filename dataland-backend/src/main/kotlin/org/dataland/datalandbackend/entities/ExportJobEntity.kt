package org.dataland.datalandbackend.entities

import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackendutils.model.ExportFileType
import org.springframework.core.io.InputStreamResource
import java.util.UUID

/**
 * Only to be stored in memory.
 */
class ExportJobEntity(
    val id: UUID,
    var fileToExport: InputStreamResource?,
    var fileType: ExportFileType,
    var frameworkName: String,
    var progressState: ExportJobProgressState = ExportJobProgressState.Pending,
    var creationTime: Long,
)
