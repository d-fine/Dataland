package org.dataland.datalandbackend.model.sme

import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.model.enums.sme.CompanyAgeBracket
import org.dataland.datalandbackend.model.enums.sme.EnergyEfficiencyBracket
import org.dataland.datalandbackend.model.enums.sme.EnergyProductionBracket
import org.dataland.datalandbackend.model.enums.sme.HeatSource
import org.dataland.datalandbackend.model.enums.sme.Industry
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the questionnaire for the SME framework
 */
@DataType("sme")
data class SmeData(
    val industry: Industry?,

    val financialYear: BigDecimal?,

    val totalRevenue: BigDecimal?,

    val researchAndDevelopmentExpenses: BigDecimal?,

    val energyEfficiencyInvestments: EnergyEfficiencyBracket?,

    val operatingCosts: BigDecimal?,

    val totalAssets: BigDecimal?,

    val yearsSinceFounded: CompanyAgeBracket?,

    val productCategoryWithHighestSales: String?,

    val productCategoryWithHighestSalesShareOfSales: BigDecimal?,

    val productCategoryWithSecondHighestSales: String?,

    val productCategoryWithSecondHighestSalesShareOfSales: BigDecimal?,

    val totalAreaCompany: BigDecimal?,

    val totalPowerConsumption: BigDecimal?,

    val totalPowerCosts: BigDecimal?,

    val useOfGreenElectricity: YesNoNa?,

    val heatingEnergyConsumption: BigDecimal?,

    val totalHeatingCosts: BigDecimal?,

    val roomWaterHeating: HeatSource?,

    val shareOwnEnergyProduction: EnergyProductionBracket?,

    val waterSewageCosts: BigDecimal?,

    val wasteDisposalCosts: BigDecimal?,

    val wasteRecyclingRate: BigDecimal?,

    val numberOfEmployees: BigDecimal?,

    val numberOfTemporaryWorkers: BigDecimal?,

    val shareOfFullTimeEmployees: BigDecimal?,

    val shareOfEmployeesSubjectToSocialSecurityContributions: BigDecimal?,

    val employeeFluctuation: BigDecimal?,

    val shareOfFemaleEmployees: BigDecimal?,

    val proportionOfFemaleEmployeesInManagement: BigDecimal?,

    val oshMeasures: YesNoNa?,

    val healthAndOldAgeOffers: YesNoNa?,

    val numberVacationDays: BigDecimal?,

    val nonProfitProjects: YesNoNa?,

)
