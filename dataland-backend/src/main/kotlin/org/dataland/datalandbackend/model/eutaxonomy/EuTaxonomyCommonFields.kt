package org.dataland.datalandbackend.model.eutaxonomy

import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Interface describing fields that are present in both
 * EuTaxonomyForFinancials and EuTaxonomyForNonFinancials to ensure naming consistency
 */
interface EuTaxonomyCommonFields {
    val fiscalYearDeviation: FiscalYearDeviation?
    val fiscalYearEnd: LocalDate?
    val scopeOfEntities: YesNoNa?
    val nfrdMandatory: YesNo?
    val euTaxonomyActivityLevelReporting: YesNo?
    val assurance: AssuranceDataPoint<AssuranceOptions>?
    val numberOfEmployees: BigDecimal?
}
