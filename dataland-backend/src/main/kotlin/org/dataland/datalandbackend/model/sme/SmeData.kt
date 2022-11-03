package org.dataland.datalandbackend.model.sme

import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.sme.Branch
import org.dataland.datalandbackend.model.enums.sme.CompanyAgeBracket
import org.dataland.datalandbackend.model.enums.sme.EnergyEfficiencyBracket
import org.dataland.datalandbackend.model.enums.sme.EnergyProductionBracket
import org.dataland.datalandbackend.model.enums.sme.HeatSource
import org.dataland.datalandbackend.model.enums.sme.LegalForm
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the questionnaire for the SME framework
 */
@DataType("sme")
data class SmeData(
    val companyName: String?,

    val companyAge: CompanyAgeBracket?,

    val companyLegalForm: LegalForm?,

    val shareOfInvestmentsForEnergyEfficiency: EnergyEfficiencyBracket?,

    val businessYear: Long?,

    val branch: Branch?,

    val renewableEnergy: YesNo?,

    val heatSource: HeatSource?,

    val shareOfSelfProducedEnergy: EnergyProductionBracket?,

    val numberOfEmployees: Long?,

    val revenue: BigDecimal?,

    val electricityConsumption: BigDecimal?,

    val workerProtectionMeasures: YesNo?,

)
