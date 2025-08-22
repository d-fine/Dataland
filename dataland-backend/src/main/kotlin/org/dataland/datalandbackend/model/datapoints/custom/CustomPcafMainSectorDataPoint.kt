package org.dataland.datalandbackend.model.datapoints.custom

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.pcaf.MainPcafSector

/**
 * --- API model ---
 * Main commercial sector of the company
 */
data class CustomPcafMainSectorDataPoint(
    @field:JsonProperty(required = true)
    override val value: MainPcafSector,
    @field:Valid()
    override val dataSource: ExtendedDocumentReference? = null,
    val provider: String? = null,
) : BaseDataPoint<MainPcafSector>
