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
    val totalAmountInTons: Number?,
    val wasteRecycleOrReuseInTons: Number?,
    val wasteDisposalInTons: Number?,
    val totalAmountInCubicMeters: Number?,
    val wasteRecycleOrReuseInCubicMeters: Number?,
    val wasteDisposalInCubicMeters: Number?,
    // TODO make sure that field names are generated correctly :totalAmountInTons
)
