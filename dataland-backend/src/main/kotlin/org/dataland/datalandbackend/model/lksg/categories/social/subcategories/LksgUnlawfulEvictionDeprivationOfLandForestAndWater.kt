package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Unlawful eviction / deprivation of land, forest and
 * water"
 */
data class LksgUnlawfulEvictionDeprivationOfLandForestAndWater(
    val unlawfulEvictionAndTakingOfLand: YesNo?,

    val unlawfulEvictionAndTakingOfLandRisk: String?,

    val unlawfulEvictionAndTakingOfLandStrategies: YesNo?,

    val unlawfulEvictionAndTakingOfLandStrategiesName: List<String>?,

    val voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure: YesNo?,
)
