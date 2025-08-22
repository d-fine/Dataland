package org.dataland.datalandbackend.model.datapoints.custom

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.dataland.datalandbackend.frameworks.pcaf.model.general.company.PcafGeneralCompanyMainPcafSectorOptions
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference

/**
 * --- API model ---
 * Main commercial sector of the company
 */
data class CustomPcafMainSectorDataPoint(
    @field:JsonProperty(required = true)
    override val value: PcafGeneralCompanyMainPcafSectorOptions,
    @field:Valid()
    override val dataSource: ExtendedDocumentReference? = null,
    val provider: String? = null,
) : BaseDataPoint<PcafGeneralCompanyMainPcafSectorOptions>
