package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.lksg.submodels.LksgWaste

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "Environmental"
 */
data class LksgEnvironmental(
    val waste: LksgWaste? = null,
)
