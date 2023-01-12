package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Waste"
 */
data class LksgWaste(
    val mercuryAndMercuryWasteHandling: YesNo? = null,

    val mercuryAndMercuryWasteHandlingPolicy: YesNo? = null,

    val chemicalHandling: YesNo? = null,

    val environmentalManagementSystem: YesNo? = null,

    val environmentalManagementSystemInternationalCertification: YesNo? = null,

    val environmentalManagementSystemNationalCertification: YesNo? = null,

    val legalRestrictedWaste: YesNo? = null,

    val legalRestrictedWasteProcesses: YesNo? = null,

    val mercuryAddedProductsHandling: YesNo? = null,

    val mercuryAddedProductsHandlingRiskOfExposure: YesNo? = null,

    val mercuryAddedProductsHandlingRiskOfDisposal: YesNo? = null,

    val mercuryAndMercuryCompoundsProductionAndUse: YesNo? = null,

    val mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: YesNo? = null,

    val persistentOrganicPollutantsProductionAndUse: YesNo? = null,

    val persistentOrganicPollutantsProductionAndUseRiskOfExposure: YesNo? = null,

    val persistentOrganicPollutantsProductionAndUseRiskOfDisposal: YesNo? = null,

    val persistentOrganicPollutantsProductionAndUseTransboundaryMovements: YesNo? = null,

    val persistentOrganicPollutantsProductionAndUseRiskForImportingState: YesNo? = null,

    val hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein: YesNo? = null,

    val hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein: YesNo? = null,

    val hazardousWasteDisposal: YesNo? = null,

    val hazardousWasteDisposalRiskOfImport: YesNo? = null,

    val hazardousAndOtherWasteImport: YesNo? = null,
)
