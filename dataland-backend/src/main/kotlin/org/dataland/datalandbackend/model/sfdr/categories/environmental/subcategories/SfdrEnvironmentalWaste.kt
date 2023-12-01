package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.validator.NonNegativeExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Waste" belonging to the category "Environmental" of the sfdr framework.
*/
data class SfdrEnvironmentalWaste(
    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val hazardousAndRadioactiveWasteInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val manufactureOfAgrochemicalPesticidesProducts: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val landDegradationDesertificationSoilSealingExposure: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val sustainableAgriculturePolicy: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val sustainableOceansAndSeasPolicy: BaseDataPoint<YesNo>? = null,

    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val nonRecycledWasteInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val threatenedSpeciesExposure: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val biodiversityProtectionPolicy: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val deforestationPolicy: BaseDataPoint<YesNo>? = null,
)
