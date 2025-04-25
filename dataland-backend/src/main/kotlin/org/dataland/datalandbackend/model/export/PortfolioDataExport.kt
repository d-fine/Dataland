package org.dataland.datalandbackend.model.export

import org.dataland.datalandbackendutils.model.ExportFileType

/**
 * Data class for the export of multiple datasets at once.
 *
 * @param fileFormat the requested file format
 * @param dataRows the list of data to be exported
 */
data class PortfolioDataExport<T>(
    val fileFormat: ExportFileType,
    val dataRows: List<SingleCompanyDataExport<T>>,
)
