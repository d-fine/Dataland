package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Contamination of soil/water/air, noise emissions,
 * excessive water consumption"
 */
data class LksgContaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption(
    val harmfulSoilChange: YesNo?,

    val soilDegradation: YesNo?,

    val soilErosion: YesNo?,

    val soilBornDiseases: YesNo?,

    val soilContamination: YesNo?,

    val soilSalinisation: YesNo?,

    val harmfulWaterPollution: YesNo?,

    val fertilisersOrPollutants: YesNo?,

    val wasteWaterFiltration: YesNo?,

    val harmfulAirPollution: YesNo?,

    val airFiltration: YesNo?,

    val harmfulNoiseEmission: YesNo?,

    val reduceNoiseEmissions: YesNo?,

    val excessiveWaterConsumption: YesNo?,

    val waterSavingMeasures: YesNo?,

    val waterSavingMeasuresName: String?,

    val pipeMaintaining: YesNo?,

    val waterSources: YesNo?,

    val contaminationMeasures: String?,
)
