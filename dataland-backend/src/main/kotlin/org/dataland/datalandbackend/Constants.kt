package org.dataland.datalandbackend

import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.springframework.beans.factory.annotation.Autowired

const val RATIO_PRECISION = 4

val PREVIEWCOMPANY_NAME = System.getenv("PREVIEWCOMPANY_NAME")

class PreviewStuff(
    @Autowired var dataManager: DataManagerInterface,
) {
    val previewCompanyId = "1"//dataManager.searchCompanies(PREVIEWCOMPANY_NAME, true).last().companyId
}
