package org.dataland.datalandbackend.model.lksg.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.lksg.ShareOfTemporaryWorkers
import org.dataland.datalandbackend.model.lksg.LksgAddress
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Master data"
 */
data class LksgMasterData(
    @field:JsonProperty(required = true)
    val dataDate: LocalDate,

    val name: String,

    val address: LksgAddress,

    val headOffice: YesNo,

    val commercialRegister: String,

    val groupOfCompanies: YesNo,

    val groupOfCompaniesName: String,

    val industry: String,

    val numberOfEmployees: BigDecimal,

    val seasonalOrMigrantWorkers: YesNo,

    val shareOfTemporaryWorkers: ShareOfTemporaryWorkers,

    val totalRevenueCurrency: String,

    val totalRevenue: BigDecimal,

    val fixedAndWorkingCapital: BigDecimal?,
)
