package org.dataland.datalandbackend.frameworks.lksg.custom

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Lksg risk positions
 */
@Schema(
    enumAsRef = true,
)
enum class RiskPosition {
    ChildLabor,
    ForcedLabor,
    Slavery,
    DisregardForOccupationalHealthOrSafety,
    DisregardForFreedomOfAssociation,
    UnequalTreatmentOfEmployment,
    WithholdingAdequateWages,
    ContaminationOfSoilWaterAirOrNoiseEmissionsOrExcessiveWaterConsumption,
    UnlawfulEvictionOrDeprivationOfLandOrForestAndWater,
    UseOfPrivatePublicSecurityForcesWithDisregardForHumanRights,
    UseOfMercuryOrMercuryWaste,
    ProductionAndUseOfPersistentOrganicPollutants,
    ExportImportOfHazardousWaste,
}
