package org.dataland.datalandbackend.frameworks.vsme.custom

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Subsidiaries class for vsme framework
 */
data class VsmeWasteClassificationObject(
    @field:JsonProperty(required = true)
    val wasteClassification: WasteClassifications,
    @field:JsonProperty(required = true)
    val typeOfWaste: String,
    val totalAmountOfWasteInTonnes: Number?,
    val wasteRecycleOrReuseInTonnes: Number?,
    val wasteDisposalInTonnes: Number?,
    val totalAmountOfWasteInCubicMeters: Number?,
    val wasteRecycleOrReuseInCubicMeters: Number?,
    val wasteDisposalInCubicMeters: Number?,
)
