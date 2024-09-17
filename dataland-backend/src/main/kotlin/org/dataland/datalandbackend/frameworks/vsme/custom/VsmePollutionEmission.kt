package org.dataland.datalandbackend.frameworks.vsme.custom

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import org.dataland.datalandbackend.model.enums.sme.ReleaseMedium

/** --- API model --- Pollution emission class for vsme framework */
data class VsmePollutionEmission(
  @field:JsonProperty(required = true) val pollutionType: String,
  val emissionInKilograms: BigDecimal? = null,
  @field:JsonProperty(required = true) val releaseMedium: ReleaseMedium,
)
