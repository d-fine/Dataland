package org.dataland.datalandbackend.model.sfdr.categories.general

import org.dataland.datalandbackend.model.sfdr.categories.general.subcategories.SfdrGeneralGeneral
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Fields of the category "General" of the sfdr framework.
 */
data class SfdrGeneral(
      @field:JsonProperty(required = true)
      val general: SfdrGeneralGeneral,
)
