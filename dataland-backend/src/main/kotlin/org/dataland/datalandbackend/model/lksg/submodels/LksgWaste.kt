package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Waste"
 */
data class LksgWaste(
    val mercuryAndMercuryWasteHandling: YesNo?,

    val mercuryAndMercuryWasteHandlingPolicy: YesNo?,

    val chemicalHandling: YesNo?,

    val environmentalManagementSystem: YesNo?,

    val environmentalManagementSystemInternationalCertification: YesNo?,

    val environmentalManagementSystemNationalCertification: YesNo?,

    val legalRestrictedWaste: YesNo?,

    val legalRestrictedWasteProcesses: YesNo?,

    val mercuryAddedProductsHandling: YesNo?,

    val mercuryAddedProductsHandlingRiskOfExposure: YesNo?,

    val mercuryAddedProductsHandlingRiskOfDisposal: YesNo?,

    val mercuryAndMercuryCompoundsProductionAndUse: YesNo?,

    val mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: YesNo?,

    val persistentOrganicPollutantsProductionAndUse: YesNo?,

    val persistentOrganicPollutantsProductionAndUseRiskOfExposure: YesNo?,

    val persistentOrganicPollutantsProductionAndUseRiskOfDisposal: YesNo?,

    val persistentOrganicPollutantsProductionAndUseTransboundaryMovements: YesNo?,

    val persistentOrganicPollutantsProductionAndUseRiskForImportingState: YesNo?,

    val hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein: YesNo?,

    val hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein: YesNo?,

    val hazardousWasteDisposal: YesNo?,

    val hazardousWasteDisposalRiskOfImport: YesNo?,

    val hazardousAndOtherWasteImport: YesNo?,
)
