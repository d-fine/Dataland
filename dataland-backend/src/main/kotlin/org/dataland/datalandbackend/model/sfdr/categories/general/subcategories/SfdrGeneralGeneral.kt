package org.dataland.datalandbackend.model.sfdr.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
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
    val dataDate: LocalDate,

    @field:JsonProperty(required = true)
    val fiscalYearDeviation: FiscalYearDeviation,

    @field:JsonProperty(required = true)
    val fiscalYearEnd: LocalDate,

    @field:Schema(example = JsonExampleFormattingConstants.REFERENCED_REPORTS_DEFAULT_VALUE)
    override val referencedReports: Map<String, CompanyReport>? = null,

    val scopeOfEntities: YesNoNa? = null,
) : FrameworkBase
