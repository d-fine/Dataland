package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.CompanyReport
import org.dataland.datalandbackend.model.FrameworkBase
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import org.dataland.datalandbackend.model.eutaxonomy.EuTaxonomyCommonFields
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * This class holds general information of EU taxonomy data
 */
data class EuTaxonomyGeneral(
    override val fiscalYearDeviation: FiscalYearDeviation?,

    override val fiscalYearEnd: LocalDate?,

    override val scopeOfEntities: YesNoNa?,

    override val nfrdMandatory: YesNo?,

    override val euTaxonomyActivityLevelReporting: YesNo?,

    override val assurance: AssuranceData?,

    override val numberOfEmployees: BigDecimal?,

    override val referencedReports: Map<String, CompanyReport>?,
) : EuTaxonomyCommonFields, FrameworkBase
