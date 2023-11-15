package org.dataland.datalandbackend.model.lksg.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.lksg.ShareOfTemporaryWorkers
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the subcategory "Master Data" belonging to the category "General" of the lksg framework.
*/
data class LksgGeneralMasterData(
    @field:JsonProperty(required = true)
    val dataDate: LocalDate,

    val headOfficeInGermany: YesNo? = null,

    val groupOfCompanies: YesNo? = null,

    val groupOfCompaniesName: String? = null,

    val industry: List<String>? = null,

    val numberOfEmployees: BigDecimal? = null,

    val seasonalOrMigrantWorkers: YesNo? = null,

    val shareOfTemporaryWorkers: ShareOfTemporaryWorkers? = null,

    val annualTotalRevenue: BigDecimal? = null,

    val totalRevenueCurrency: String? = null,

    val fixedAndWorkingCapital: BigDecimal? = null,
)
