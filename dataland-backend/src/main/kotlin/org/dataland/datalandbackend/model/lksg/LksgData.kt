package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.lksg.categories.general.LksgGeneral
import org.dataland.datalandbackend.model.lksg.categories.governance.LksgGovernance
import org.dataland.datalandbackend.model.lksg.categories.social.LksgSocial
import org.dataland.datalandbackend.model.lksg.categories.environmental.LksgEnvironmental
import org.dataland.datalandbackend.annotations.DataType
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Fields of the lksg framework.
*/
@DataType("lksg")
data class LksgData(
      @field:JsonProperty(required = true)
      val general: LksgGeneral,

      val governance: LksgGovernance? = null,

      val social: LksgSocial? = null,

      val environmental: LksgEnvironmental? = null,
)
