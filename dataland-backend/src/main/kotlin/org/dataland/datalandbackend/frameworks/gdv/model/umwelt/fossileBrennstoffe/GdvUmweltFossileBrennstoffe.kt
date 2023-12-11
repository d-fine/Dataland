package org.dataland.datalandbackend.frameworks.gdv.model.umwelt.fossileBrennstoffe

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.gdv.YearlyTimeseriesData

/**
 * The data-model for the FossileBrennstoffe section
 */
data class GdvUmweltFossileBrennstoffe(
    val einnahmenAusFossilenBrennstoffen: YesNo?,
    val berichterstattungEinnahmenAusFossilenBrennstoffen: YearlyTimeseriesData<BerichterstattungEinnahmenAusFossilenBrennstoffenValues?>?,
)
