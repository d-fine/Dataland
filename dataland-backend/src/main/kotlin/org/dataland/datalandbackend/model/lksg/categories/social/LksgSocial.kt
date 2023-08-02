package org.dataland.datalandbackend.model.lksg.categories.social

import org.dataland.datalandbackend.model.lksg.categories.social.subcategories.LksgSocialChildLabor
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories.LksgSocialForcedLaborSlavery
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories.LksgSocialWithholdingAdequateWages
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories.LksgSocialDisregardForOccupationalHealthSafety
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories.LksgSocialDisregardForFreedomOfAssociation
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories.LksgSocialUnequalTreatmentOfEmployment
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories.LksgSocialContaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories.LksgSocialUnlawfulEvictionDeprivationOfLandForestAndWater
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories.LksgSocialUseOfPrivatePublicSecurityForcesWithDisregardForHumanRights

/**
 * --- API model ---
 * Fields of the category "Social" of the lksg framework.
*/
data class LksgSocial(
      val childLabor: LksgSocialChildLabor? = null,

      val forcedLaborSlavery: LksgSocialForcedLaborSlavery? = null,

      val withholdingAdequateWages: LksgSocialWithholdingAdequateWages? = null,

      val disregardForOccupationalHealthSafety: LksgSocialDisregardForOccupationalHealthSafety? = null,

      val disregardForFreedomOfAssociation: LksgSocialDisregardForFreedomOfAssociation? = null,

      val unequalTreatmentOfEmployment: LksgSocialUnequalTreatmentOfEmployment? = null,

      val contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption: LksgSocialContaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption? = null,

      val unlawfulEvictionDeprivationOfLandForestAndWater: LksgSocialUnlawfulEvictionDeprivationOfLandForestAndWater? = null,

      val useOfPrivatePublicSecurityForcesWithDisregardForHumanRights: LksgSocialUseOfPrivatePublicSecurityForcesWithDisregardForHumanRights? = null,
)
