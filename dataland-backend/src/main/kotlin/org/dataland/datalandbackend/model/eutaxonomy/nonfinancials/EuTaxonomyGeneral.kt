package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.dataland.datalandbackend.interfaces.frameworks.EuTaxonomyCommonFields
import org.dataland.datalandbackend.interfaces.frameworks.FrameworkBase
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceDataPoint
import org.dataland.datalandbackend.model.eutaxonomy.ConstantParameters
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * This class holds general information of EU taxonomy data
 */
data class EuTaxonomyGeneral(
    override val fiscalYearDeviation: FiscalYearDeviation?,

    @field:JsonProperty(required = true)
    override val fiscalYearEnd: LocalDate?,

    override val scopeOfEntities: YesNoNa?,

    override val nfrdMandatory: YesNo?,

    override val euTaxonomyActivityLevelReporting: YesNo?,

    @field:Valid
    override val assurance: AssuranceDataPoint?,

    @Min(ConstantParameters.PERCENT_MINIMUM)
    override val numberOfEmployees: BigDecimal?,

    @field:Schema(example = JsonExampleFormattingConstants.REFERENCED_REPORTS_DEFAULT_VALUE)
    override val referencedReports: Map<String, CompanyReport>?,
) : EuTaxonomyCommonFields, FrameworkBase
