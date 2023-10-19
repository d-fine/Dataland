package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.interfaces.frameworks.EuTaxonomyCommonFields
import org.dataland.datalandbackend.interfaces.frameworks.FrameworkBase
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceDataPoint
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
) : EuTaxonomyCommonFields, FrameworkBase
