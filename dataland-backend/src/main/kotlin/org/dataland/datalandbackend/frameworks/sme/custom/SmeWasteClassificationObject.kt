package org.dataland.datalandbackend.frameworks.sme.custom

/**
 * --- API model ---
 * Subsidiaries class for vsme framework
 */
data class SmeWasteClassificationObject(
    val wasteClassification: WasteClassifications,
    val typeWaste: String?,
    val totalAmountTons: Number?,
    val wasteRecycleOrReuseTons: Number?,
    val wasteDisposalTons: Number?,
    val totalAmountCubicMeters: Number?,
    val wasteRecycleOrReuseCubicMeters: Number?,
    val wasteDisposalCubicMeters: Number?,
)
