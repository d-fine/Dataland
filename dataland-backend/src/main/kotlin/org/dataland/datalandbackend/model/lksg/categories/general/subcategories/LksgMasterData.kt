package org.dataland.datalandbackend.model.lksg.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.lksg.ShareOfTemporaryWorkers
import org.dataland.datalandbackend.model.lksg.categories.general.LksgAddress
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Master data"
 */
data class LksgMasterData(
    @field:JsonProperty(required = true)
    val dataDate: LocalDate,

    @field:JsonProperty(required = true)
    val name: String,

    @field:JsonProperty(required = true)
    val address: LksgAddress,

    @field:JsonProperty(required = true)
    val headOffice: YesNo,

    @field:JsonProperty(required = true)
    val commercialRegister: String,

    @field:JsonProperty(required = true)
    val groupOfCompanies: YesNo,

    @field:JsonProperty(required = true)
    val groupOfCompaniesName: String,

    @field:JsonProperty(required = true)
    val industry: String,

    @field:JsonProperty(required = true)
    val numberOfEmployees: BigDecimal,

    @field:JsonProperty(required = true)
    val seasonalOrMigrantWorkers: YesNo,

    @field:JsonProperty(required = true)
    val shareOfTemporaryWorkers: ShareOfTemporaryWorkers,

    @field:JsonProperty(required = true)
    val totalRevenueCurrency: String,

    @field:JsonProperty(required = true)
    val totalRevenue: BigDecimal,

    val fixedAndWorkingCapital: BigDecimal?,
)
