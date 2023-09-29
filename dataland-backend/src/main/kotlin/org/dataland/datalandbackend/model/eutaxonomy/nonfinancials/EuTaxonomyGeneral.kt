package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.CompanyReport
import org.dataland.datalandbackend.interfaces.FrameworkBaseInterface
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceDataPoint
import org.dataland.datalandbackend.interfaces.EuTaxonomyCommonFieldsInterface
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

    override val assurance: AssuranceDataPoint?,

    override val numberOfEmployees: BigDecimal?,

    override val referencedReports: Map<String, CompanyReport>?,
) : EuTaxonomyCommonFieldsInterface, FrameworkBaseInterface
