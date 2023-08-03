package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Unlawful eviction/deprivation of land, forest and water" belonging to the category
 * "Social" of the Lksg framework.
*/
data class LksgSocialUnlawfulEvictionDeprivationOfLandForestAndWater(
      val unlawfulEvictionAndTakingOfLand: YesNo? = null,

      val unlawfulEvictionAndTakingOfLandRisk: String? = null,

      val unlawfulEvictionAndTakingOfLandStrategies: YesNo? = null,

      val unlawfulEvictionAndTakingOfLandStrategiesName: String? = null,

      val voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure: YesNo? = null,
)
