package org.dataland.datalandbackend.model.export
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.ExportFileType

/**
 * DTO representing options for data export
 */
data class ExportOptions(
    val dataType: DataType,
    val exportFileType: ExportFileType,
    val keepValueFieldsOnly: Boolean,
    val includeAliases: Boolean,
)
