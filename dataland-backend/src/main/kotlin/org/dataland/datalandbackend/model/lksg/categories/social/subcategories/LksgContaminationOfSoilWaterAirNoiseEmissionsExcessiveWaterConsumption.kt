package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Contamination of soil/water/air, noise emissions,
 * excessive water consumption"
 */
data class LksgContaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption(
        val harmfulSoilChange: BaseDataPoint<YesNo>?,

        val soilDegradation: BaseDataPoint<YesNo>?,

        val soilErosion: BaseDataPoint<YesNo>?,

        val soilBornDiseases: BaseDataPoint<YesNo>?,

        val soilContamination: BaseDataPoint<YesNo>?,

        val soilSalinisation: BaseDataPoint<YesNo>?,

        val harmfulWaterPollution: BaseDataPoint<YesNo>?,

        val fertilisersOrPollutants: BaseDataPoint<YesNo>?,

        val wasteWaterFiltration: BaseDataPoint<YesNo>?,

        val harmfulAirPollution: BaseDataPoint<YesNo>?,

        val airFiltration: BaseDataPoint<YesNo>?,

        val harmfulNoiseEmission: BaseDataPoint<YesNo>?,

        val reduceNoiseEmissions: BaseDataPoint<YesNo>?,

        val excessiveWaterConsumption: BaseDataPoint<YesNo>?,

        val waterSavingMeasures: BaseDataPoint<YesNo>?,

        val waterSavingMeasuresName: BaseDataPoint<String>?,

        val pipeMaintaining: BaseDataPoint<YesNo>?,

        val waterSources: BaseDataPoint<YesNo>?,

        val contaminationMeasures: BaseDataPoint<String>?,
)
