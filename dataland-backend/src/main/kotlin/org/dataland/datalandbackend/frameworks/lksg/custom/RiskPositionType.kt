package org.dataland.datalandbackend.frameworks.lksg.custom

import io.swagger.v3.oas.annotations.media.Schema

/**
 * A risk position
 */
@Schema(
    enumAsRef = true,
)
enum class RiskPositionType {
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
