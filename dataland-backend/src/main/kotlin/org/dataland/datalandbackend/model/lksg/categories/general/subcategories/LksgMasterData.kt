package org.dataland.datalandbackend.model.lksg.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.lksg.ShareOfTemporaryWorkers
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Master data"
 */
data class LksgMasterData(
    @field:JsonProperty(required = true)
    val dataDate: BaseDataPoint<LocalDate>,

    val headOfficeInGermany: BaseDataPoint<YesNo>?,

    val groupOfCompanies: BaseDataPoint<YesNo>?,

    val groupOfCompaniesName: BaseDataPoint<String>?,

    val industry: BaseDataPoint<List<String>>?,

    val numberOfEmployees: BaseDataPoint<BigDecimal>?,

    val seasonalOrMigrantWorkers: BaseDataPoint<YesNo>?,

    val shareOfTemporaryWorkers: BaseDataPoint<ShareOfTemporaryWorkers>?,

    val totalRevenueCurrency: BaseDataPoint<String>?,

    val totalRevenue: BaseDataPoint<BigDecimal>?,

    val fixedAndWorkingCapital: BaseDataPoint<BigDecimal>?,
)
