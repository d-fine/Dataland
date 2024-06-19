package org.dataland.datalandbackend.frameworks.vsme.custom

/**
 * --- API model ---
 * Subsidiaries class for vsme framework
 */
data class VsmeWasteClassificationObject(
    val wasteClassification: WasteClassifications,
    val typeOfWaste: String?,
    val totalAmountTons: Number?,
    val wasteRecycleOrReuseTons: Number?,
    val wasteDisposalTons: Number?,
    val totalAmountCubicMeters: Number?,
    val wasteRecycleOrReuseCubicMeters: Number?,
    val wasteDisposalCubicMeters: Number?,
)
