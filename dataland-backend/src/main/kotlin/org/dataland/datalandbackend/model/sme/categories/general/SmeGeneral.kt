package org.dataland.datalandbackend.model.sme.categories.general

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.sme.categories.general.subcategories.SmeGeneralBasicInformation
import org.dataland.datalandbackend.model.sme.categories.general.subcategories.SmeGeneralBusinessNumbers

/**
 * --- API model ---
 * Fields of the category "General" of the sme framework.
*/
data class SmeGeneral(
    @field:JsonProperty(required = true)
    val basicInformation: SmeGeneralBasicInformation,

    val businessNumbers: SmeGeneralBusinessNumbers? = null,
)
