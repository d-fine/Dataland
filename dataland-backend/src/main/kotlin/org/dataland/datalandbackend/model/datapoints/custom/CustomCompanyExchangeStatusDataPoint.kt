package org.dataland.datalandbackend.model.datapoints.custom

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.dataland.datalandbackend.frameworks.pcaf.model.general.company.PcafGeneralCompanyCompanyExchangeStatusOptions
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference

/**
 * --- API model ---
 * "Listed/Unlisted"-exchange status of the company.
 */
data class CustomCompanyExchangeStatusDataPoint(
    @field:JsonProperty(required = true)
    override val value: PcafGeneralCompanyCompanyExchangeStatusOptions,
    @field:Valid()
    override val dataSource: ExtendedDocumentReference? = null,
    val provider: String? = null,
) : BaseDataPoint<PcafGeneralCompanyCompanyExchangeStatusOptions>
