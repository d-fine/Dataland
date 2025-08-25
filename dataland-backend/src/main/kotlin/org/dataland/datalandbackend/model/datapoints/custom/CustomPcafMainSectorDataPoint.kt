package org.dataland.datalandbackend.model.datapoints.custom

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.dataland.datalandbackend.frameworks.pcaf.model.general.company.PcafGeneralCompanyMainPcafSectorOptions
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Main commercial sector of the company
 */
data class CustomPcafMainSectorDataPoint(
    @field:JsonProperty(required = true)
    override val value: PcafGeneralCompanyMainPcafSectorOptions,
    override val quality: QualityOptions? = null,
    override val comment: String? = null,
    @field:Valid()
    override val dataSource: ExtendedDocumentReference? = null,
    val provider: String? = null,
) : ExtendedDataPoint<PcafGeneralCompanyMainPcafSectorOptions>
