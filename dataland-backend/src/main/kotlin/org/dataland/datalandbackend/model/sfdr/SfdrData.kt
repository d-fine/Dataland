package org.dataland.datalandbackend.model.sfdr

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.sfdr.categories.environmental.SfdrEnvironmental
import org.dataland.datalandbackend.model.sfdr.categories.general.SfdrGeneral
import org.dataland.datalandbackend.model.sfdr.categories.social.SfdrSocial

/**
 * --- API model ---
 * Fields of the sfdr framework.
*/
@DataType("sfdr")
data class SfdrData(
    @field:JsonProperty(required = true)
    val general: SfdrGeneral,

    @field:Valid
    val environmental: SfdrEnvironmental? = null,

    @field:Valid
    val social: SfdrSocial? = null,
)
