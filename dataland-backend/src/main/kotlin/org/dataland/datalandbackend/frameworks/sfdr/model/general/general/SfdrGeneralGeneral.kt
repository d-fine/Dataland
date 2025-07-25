// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.sfdr.model.general.general

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import org.dataland.datalandbackend.frameworks.sfdr.model.general.general.SfdrGeneralGeneralFiscalYearDeviationOptions
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants
import java.time.LocalDate

/**
 * The data-model for the General section
 */
@Suppress("MaxLineLength")
data class SfdrGeneralGeneral(
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """The year for which the data is reported.""",
        example = """ "2007-03-05"  """,
    )
    val dataDate: LocalDate? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the fiscal year deviate from the calendar year?""",
        example = """ "Option 1"  """,
    )
    val fiscalYearDeviation: SfdrGeneralGeneralFiscalYearDeviationOptions? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """The date the fiscal year ends.""",
        example = """ "2007-03-05"  """,
    )
    val fiscalYearEnd: LocalDate? = null,
    @field:Schema(example = JsonExampleFormattingConstants.REFERENCED_REPORTS_DEFAULT_VALUE)
    @field:Valid()
    val referencedReports: Map<String, CompanyReport>? = null,
)
