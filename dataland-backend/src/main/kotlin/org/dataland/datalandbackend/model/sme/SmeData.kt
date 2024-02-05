package org.dataland.datalandbackend.model.sme

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.sme.categories.general.SmeGeneral
import org.dataland.datalandbackend.model.sme.categories.insurances.SmeInsurances
import org.dataland.datalandbackend.model.sme.categories.power.SmePower
import org.dataland.datalandbackend.model.sme.categories.production.SmeProduction

/**
 * --- API model ---
 * Fields of the sme framework.
*/
@Suppress("MagicNumber")
@DataType("sme", 5)
data class SmeData(
    @field:JsonProperty(required = true)
    val general: SmeGeneral,

    val production: SmeProduction? = null,

    val power: SmePower? = null,

    val insurances: SmeInsurances? = null,
)
