package org.dataland.datalandbackend.model.sme

import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.sme.Branch
import org.dataland.datalandbackend.model.enums.sme.CompanyAgeBracket
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the questionnaire for the SME framework
 */
@DataType("sme")
data class SmeData(
    val companyName: String?,

    val companyAge: CompanyAgeBracket?,

    val businessYear: Long?,

    val branch: Branch?,

    val numberOfEmployees: Long?,

    val revenue: BigDecimal?,

    val electricityConsumption: BigDecimal?,

    val workerProtectionMeasures: YesNo?,

)
