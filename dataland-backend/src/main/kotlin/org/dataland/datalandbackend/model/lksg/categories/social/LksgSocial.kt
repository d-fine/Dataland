package org.dataland.datalandbackend.model.lksg.categories.social

import org.dataland.datalandbackend.model.lksg.categories.social.subcategories
    .LksgChildLabor
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories
    .LksgContaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories
    .LksgDisregardForFreedomOfAssociation
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories
    .LksgDisregardForOccupationalHealthSafety
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories
    .LksgForcedLaborSlavery
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories
    .LksgUnequalTreatmentOfEmployment
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories
    .LksgUnlawfulEvictionDeprivationOfLandForestAndWater
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories
    .LksgUseOfPrivatePublicSecurityForcesWithDisregardForHumanRights
import org.dataland.datalandbackend.model.lksg.categories.social.subcategories
    .LksgWithholdingAdequateWages

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "Social"
 */
data class LksgSocial(
    val childLabor: LksgChildLabor?,

    val forcedLaborSlavery: LksgForcedLaborSlavery?,

    val withholdingAdequateWages: LksgWithholdingAdequateWages?,

    val disregardForOccupationalHealthSafety: LksgDisregardForOccupationalHealthSafety?,

    val disregardForFreedomOfAssociation: LksgDisregardForFreedomOfAssociation?,

    val unequalTreatmentOfEmployment: LksgUnequalTreatmentOfEmployment?,

    val contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption:
    LksgContaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption?,

    val unlawfulEvictionDeprivationOfLandForestAndWater: LksgUnlawfulEvictionDeprivationOfLandForestAndWater?,

    val useOfPrivatePublicSecurityForcesWithDisregardForHumanRights:
    LksgUseOfPrivatePublicSecurityForcesWithDisregardForHumanRights?,
)
