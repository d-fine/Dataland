package org.dataland.datalandbackend.frameworks.gdv.model

import org.dataland.datalandbackend.frameworks.gdv.model.general.GdvGeneral
import org.dataland.datalandbackend.frameworks.gdv.model.allgemein.GdvAllgemein
import org.dataland.datalandbackend.annotations.DataType

/**
 * The root data-model for the Gdv Framework
 */
@DataType("gdv")
data class GdvData(
    val general: GdvGeneral?,
    val allgemein: GdvAllgemein?,
)
