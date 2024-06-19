package org.dataland.datalandbackend.frameworks.vsme.custom

/**
 * --- API model ---
 * Subsidiaries class for vsme framework
 */
data class VsmeWasteClassificationObject(
    val wasteClassification: WasteClassifications,
    val typeOfWaste: String?,
    val totalAmountInTons: Number?,
    val wasteRecycleOrReuseInTons: Number?,
    val wasteDisposalInTons: Number?,
    val totalAmountInCubicMeters: Number?,
    val wasteRecycleOrReuseInCubicMeters: Number?,
    val wasteDisposalInCubicMeters: Number?,
)
