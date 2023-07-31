package org.dataland.datalandbackend.model.sfdr.categories.general

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.sfdr.categories.general.subcategories.SfdrGeneralGeneral

/**
 * --- API model ---
 * Fields of the category "General" of the sfdr framework.
 */
data class SfdrGeneral(
    @field:JsonProperty(required = true)
    val general: SfdrGeneralGeneral,
)
