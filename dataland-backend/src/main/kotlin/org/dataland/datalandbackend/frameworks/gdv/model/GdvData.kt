package org.dataland.datalandbackend.frameworks.gdv.model

import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.GdvAllgemein
import org.dataland.datalandbackend.frameworks.gdv.model.general.GdvGeneral
import org.dataland.datalandbackend.frameworks.gdv.model.soziales.GdvSoziales
import org.dataland.datalandbackend.frameworks.gdv.model.umwelt.GdvUmwelt
import org.dataland.datalandbackend.frameworks.gdv.model.unternehmensfuehrungGovernance.GdvUnternehmensfuehrungGovernance

/**
 * The root data-model for the Gdv Framework
 */
@DataType("gdv")
data class GdvData(
    val general: GdvGeneral?,
    val allgemein: GdvAllgemein?,
    val umwelt: GdvUmwelt?,
    val soziales: GdvSoziales?,
    val unternehmensfuehrungGovernance: GdvUnternehmensfuehrungGovernance?,
)
