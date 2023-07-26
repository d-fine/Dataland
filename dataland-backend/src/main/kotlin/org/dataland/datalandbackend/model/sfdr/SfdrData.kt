package org.dataland.datalandbackend.model.sfdr

import org.dataland.datalandbackend.model.sfdr.categories.general.SfdrGeneral
import org.dataland.datalandbackend.model.sfdr.categories.environmental.SfdrEnvironmental
import org.dataland.datalandbackend.model.sfdr.categories.social.SfdrSocial
import org.dataland.datalandbackend.annotations.DataType
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Fields of the sfdr framework.
 */

@DataType("sfdr")
data class SfdrData(
    @JsonProperty(value = "general", required = true)
    val general: SfdrGeneral,

    val environmental: SfdrEnvironmental? = null,

    val social: SfdrSocial? = null,
)
