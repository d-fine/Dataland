// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.esgquestionnaire.model.unternehmensfuehrungGovernance.sonstige

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigInteger

/**
 * The data-model for the Sonstige section
 */
data class EsgQuestionnaireUnternehmensfuehrunggovernanceSonstige(
    val wirtschaftspruefer: String? = null,
    val trennungVonCeoOderVorsitzenden: YesNo? = null,
    val amtszeitBisZurTrennung: BigInteger? = null,
)
