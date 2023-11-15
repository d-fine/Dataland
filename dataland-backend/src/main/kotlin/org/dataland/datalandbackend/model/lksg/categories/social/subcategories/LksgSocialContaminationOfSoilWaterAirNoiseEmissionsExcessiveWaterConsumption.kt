package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Contamination of soil/water/air, noise emissions, excessive water consumption"
 * belonging to the category "Social" of the Lksg framework.
*/
data class LksgSocialContaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption(
    val harmfulSoilImpact: YesNo? = null,

    val soilDegradation: YesNo? = null,

    val soilErosion: YesNo? = null,

    val soilBorneDiseases: YesNo? = null,

    val soilContamination: YesNo? = null,

    val soilSalinization: YesNo? = null,

    val harmfulWaterPollution: YesNo? = null,

    val fertilizersOrPollutants: YesNo? = null,

    val wasteWaterFiltration: YesNo? = null,

    val harmfulAirPollution: YesNo? = null,

    val airFiltration: YesNo? = null,

    val harmfulNoiseEmission: YesNo? = null,

    val reduceNoiseEmissions: YesNo? = null,

    val excessiveWaterConsumption: YesNo? = null,

    val waterSavingMeasures: YesNo? = null,

    val waterSavingMeasuresName: String? = null,

    val pipeMaintaining: YesNo? = null,

    val waterSources: YesNo? = null,

    val contaminationMeasures: String? = null,
)
