package org.dataland.datalandbackend.interfaces.frameworks

import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.AssuranceDataPoint
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
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
    val assurance: AssuranceDataPoint?
    val numberOfEmployees: BigDecimal?
}
