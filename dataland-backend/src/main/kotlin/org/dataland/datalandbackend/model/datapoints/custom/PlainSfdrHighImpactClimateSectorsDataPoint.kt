package org.dataland.datalandbackend.model.datapoints.custom

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.dataland.datalandbackend.frameworks.sfdr.custom.HighImpactClimateSector
import org.dataland.datalandbackend.frameworks.sfdr.custom.SfdrHighImpactClimateSectorEnergyConsumption
import org.dataland.datalandbackend.interfaces.documents.BaseDocumentReference
import org.dataland.datalandbackend.model.datapoints.DataPointWithDocumentReference

/**
 * --- API model ---
 * Data point for the energy consumption of high impact climate sectors
 */
class PlainSfdrHighImpactClimateSectorsDataPoint
    @JsonCreator
    constructor(
        @JsonValue val value: Map<HighImpactClimateSector, SfdrHighImpactClimateSectorEnergyConsumption>,
    ) : DataPointWithDocumentReference {
        override fun getAllDocumentReferences(): List<BaseDocumentReference> =
            value.values.flatMap {
                listOfNotNull(
                    it.highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue?.dataSource,
                    it.highImpactClimateSectorEnergyConsumptionInGWh?.dataSource,
                )
            }
    }
