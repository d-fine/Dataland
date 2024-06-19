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
    val totalAmountTons: Number?,
    val wasteRecycleOrReuseTons: Number?,
    val wasteDisposalTons: Number?,
    val totalAmountCubicMeters: Number?,
    val wasteRecycleOrReuseCubicMeters: Number?,
    val wasteDisposalCubicMeters: Number?,
    // TODO make sure that field names are generated correctly :totalAmountInTons
)
