package org.dataland.datalandbackend.model.sfdr

import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.CompanyReport

/**
 * --- API model ---
 * Data class containing all fields of the questionnaire for the SFDR framework
 */
@DataType("sfdr")
data class SfdrData(
    val socialData: SocialData? = null,

    val environmentalData: EnvironmentalData? = null,

    val referencedReports: Map<String, CompanyReport>? = null,
)
