package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Unlawful eviction / deprivation of land, forest and
 * water"
 */
data class LksgUnlawfulEvictionDeprivationOfLandForestAndWater(
        val unlawfulEvictionAndTakingOfLand: BaseDataPoint<YesNo>?,

        val unlawfulEvictionAndTakingOfLandRisk: BaseDataPoint<String>?,

        val unlawfulEvictionAndTakingOfLandStrategies: BaseDataPoint<YesNo>?,

        val unlawfulEvictionAndTakingOfLandStrategiesName: BaseDataPoint<String>?,

        val voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure: BaseDataPoint<YesNo>?,
)
