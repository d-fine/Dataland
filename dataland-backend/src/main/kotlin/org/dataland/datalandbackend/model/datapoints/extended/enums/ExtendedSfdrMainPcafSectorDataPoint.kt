package org.dataland.datalandbackend.model.datapoints.extended.enums

import jakarta.validation.Valid
import org.dataland.datalandbackend.frameworks.sfdr.model.general.company.SfdrGeneralCompanyMainPcafSectorOptions
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Main commercial sector of the company, as reported in the SFDR framework
 */
data class ExtendedSfdrMainPcafSectorDataPoint(
    override val value: SfdrGeneralCompanyMainPcafSectorOptions? = null,
    override val quality: QualityOptions? = null,
    override val comment: String? = null,
    @field:Valid
    override val dataSource: ExtendedDocumentReference? = null,
) : ExtendedDataPoint<SfdrGeneralCompanyMainPcafSectorOptions>
