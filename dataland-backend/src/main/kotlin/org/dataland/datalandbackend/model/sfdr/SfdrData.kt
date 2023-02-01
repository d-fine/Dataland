package org.dataland.datalandbackend.model.sfdr

import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.CompanyReport

/**
 * --- API model ---
 * Data class containing all fields of the questionnaire for the SFDR framework
 */
@DataType("sfdr")
data class SfdrData(
    val social: SfdrSocial?,

    val environmental: SfdrEnvironmental?,

    val referencedReports: Map<String, CompanyReport>?,
)
