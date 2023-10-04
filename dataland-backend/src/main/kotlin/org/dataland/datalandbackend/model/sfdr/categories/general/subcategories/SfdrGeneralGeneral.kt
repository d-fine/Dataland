package org.dataland.datalandbackend.model.sfdr.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
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

    val referencedReports: Map<String, CompanyReport>? = null,

    val scopeOfEntities: YesNoNa? = null,
)
