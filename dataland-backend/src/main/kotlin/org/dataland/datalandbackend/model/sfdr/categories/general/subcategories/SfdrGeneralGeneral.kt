package org.dataland.datalandbackend.model.sfdr.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import org.dataland.datalandbackend.interfaces.frameworks.FrameworkBase
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the subcategory "General" belonging to the category "General" of the sfdr framework.
 */
data class SfdrGeneralGeneral(
    @field:JsonProperty(required = true)
    @Pattern(regexp = "^(?<year>\\d{4})(?<sep>[^\\w\\s])(?<month>1[0-2]|0[1-9])\\k<sep>(?<day>0[1-9]|[12][0-9]|" +
            "(11\\k<sep>|[^1][4-9]\\k<sep>)30|(1[02]\\k<sep>|[^1][13578]\\k<sep>)3[01])")
    val dataDate: LocalDate,

    @field:JsonProperty(required = true)
    @Pattern(regexp = "^(?<year>\\d{4})(?<sep>[^\\w\\s])(?<month>1[0-2]|0[1-9])\\k<sep>(?<day>0[1-9]|[12][0-9]|" +
            "(11\\k<sep>|[^1][4-9]\\k<sep>)30|(1[02]\\k<sep>|[^1][13578]\\k<sep>)3[01])")
    val fiscalYearDeviation: FiscalYearDeviation,

    @field:JsonProperty(required = true)
    @Pattern(regexp = "^(?<year>\\d{4})(?<sep>[^\\w\\s])(?<month>1[0-2]|0[1-9])\\k<sep>(?<day>0[1-9]|[12][0-9]|" +
            "(11\\k<sep>|[^1][4-9]\\k<sep>)30|(1[02]\\k<sep>|[^1][13578]\\k<sep>)3[01])")
    val fiscalYearEnd: LocalDate,

    @field:Schema(example = JsonExampleFormattingConstants.REFERENCED_REPORTS_DEFAULT_VALUE)
    override val referencedReports: Map<String, CompanyReport>? = null,

    val scopeOfEntities: YesNoNa? = null,
) : FrameworkBase
