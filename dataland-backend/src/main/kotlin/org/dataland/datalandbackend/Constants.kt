package org.dataland.datalandbackend

import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

const val RATIO_PRECISION = 4

val PREVIEWCOMPANY_NAME = "Adidas AG"//System.getenv("PREVIEWCOMPANY_NAME")

@Component("PreviewStuff")
class PreviewStuff(
    @Autowired var dataManager: DataManagerInterface,
) {
    fun isCompanyPublic(companyId:String) : Boolean {
        val searchResult = dataManager.searchCompanies(PREVIEWCOMPANY_NAME, true)
        return if (searchResult.isEmpty()) {
            false
        }
        //else if {searchResult.size > 1}
        else companyId == searchResult.last().companyId
    }
}
