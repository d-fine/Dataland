package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import java.time.LocalDate
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.CompanyReport

/**
 * --- API model ---
 * Fields of the subcategory "General" belonging to the category "Social" of the sfdr framework.
 */
data class SfdrSocialGeneral(
    val fiscalYear: String? = null,

    @field:JsonProperty(required = true)
    val fiscalYearEnd: LocalDate,

    val reports: Map<String, CompanyReport>?,

    val scopeOfEntities: YesNoNa? = null,
)
