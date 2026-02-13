package org.dataland.datalandbackend.model.enums.export

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Possible progress states for export jobs.
 */
@Schema(
    enumAsRef = true,
)
enum class ExportJobProgressState { Pending, Success, Failure }
