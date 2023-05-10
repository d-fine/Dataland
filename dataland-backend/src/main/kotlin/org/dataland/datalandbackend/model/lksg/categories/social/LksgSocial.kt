package org.dataland.datalandbackend.model.lksg.categories.social

import org.dataland.datalandbackend.model.lksg.categories.social.subcategories.*

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
