package org.dataland.datalandbackend.interfaces.export

import org.dataland.datalandbackendutils.model.ExportFileType

/**
 *
 */
interface ExportLatestRequestData {
    val companyIds: List<String>
    val fileFormat: ExportFileType
}
