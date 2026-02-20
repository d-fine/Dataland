package org.dataland.datalandbackend.model.export
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.ExportFileType

/**
 * DTO representing options for data export
 */
data class ExportOptions(
    /** The datatype specifying the framework to be used for export */
    val dataType: DataType,
    /** The file type to be exported */
    val exportFileType: ExportFileType,
    /** If true, non metadata fields like referenced documents are stripped */
    val keepValueFieldsOnly: Boolean,
    /** If true, human-readable names are used if available */
    val includeAliases: Boolean,
)
