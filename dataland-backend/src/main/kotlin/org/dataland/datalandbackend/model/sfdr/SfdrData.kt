package org.dataland.datalandbackend.model.sfdr

import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.CompanyReport
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the questionnaire for the SFDR framework
 */
@DataType("sfdr")
data class SfdrData(
    val fiscalYear: FiscalYearDeviation?,

    val fiscalYearEnd: LocalDate?,

    val annualReport: String?,

    val groupLevelAnnualReport: YesNoNa?,

    val annualReportDate: LocalDate?,

    val annualReportCurrency: String?,

    val sustainabilityReport: String?,

    val groupLevelSustainabilityReport: YesNoNa?,

    val sustainabilityReportDate: LocalDate?,

    val sustainabilityReportCurrency: String?,

    val integratedReport: String?,

    val groupLevelIntegratedReport: YesNoNa?,

    val integratedReportDate: LocalDate?,

    val integratedReportCurrency: String?,

    val esefReport: String?,

    val groupLevelEsefReport: YesNoNa?,

    val esefReportDate: LocalDate?,

    val esefReportCurrency: String?,

    val scopeOfEntities: YesNoNa?,

    val scope1: DataPoint<BigDecimal>?,

    val scope2: DataPoint<BigDecimal>?,

    val scope3: DataPoint<BigDecimal>?,

    val enterpriseValue: DataPoint<BigDecimal>?,

    val totalRevenue: DataPoint<BigDecimal>?,

    val fossilFuelSectorExposure: DataPoint<YesNo>?,

    val renewableEnergyProduction: DataPoint<BigDecimal>?,

    val renewableEnergyConsumption: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumption: DataPoint<BigDecimal>?,

    val nonRenewableEnergyProduction: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceA: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceB: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceC: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceD: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceE: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceF: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceG: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceH: DataPoint<BigDecimal>?,

    val highImpactClimateSectorEnergyConsumptionNaceL: DataPoint<BigDecimal>?,

    val totalHighImpactClimateSectorEnergyConsumption: DataPoint<BigDecimal>?,

    val primaryForestAndWoodedLandOfNativeSpeciesExposure: DataPoint<YesNo>?,

    val protectedAreasExposure: DataPoint<YesNo>?,

    val rareOrEndangeredEcosystemsExposure: DataPoint<YesNo>?,

    val emissionsToWater: DataPoint<BigDecimal>?,

    val hazardousWaste: DataPoint<BigDecimal>?,

    val humanRightsPolicy: DataPoint<YesNo>?,

    val humanRightsLegalProceedings: DataPoint<YesNo>?,

    val iloCoreLabourStandards: DataPoint<YesNo>?,

    val environmentalPolicy: DataPoint<YesNo>?,

    val corruptionLegalProceedings: DataPoint<YesNo>?,

    val transparencyDisclosurePolicy: DataPoint<YesNo>?,

    val humanRightsDueDiligencePolicy: DataPoint<YesNo>?,

    val childForcedDiscriminationPolicy: DataPoint<YesNo>?,

    val iso14001: DataPoint<YesNo>?,

    val briberyCorruptionPolicy: DataPoint<YesNo>?,

    val fairBusinessMarketingAdvertisingPolicy: DataPoint<YesNo>?,

    val technologiesExpertiseTransferPolicy: DataPoint<YesNo>?,

    val fairCompetitionPolicy: DataPoint<YesNo>?,

    val violationOfTaxRulesAndRegulation: DataPoint<YesNo>?,

    val unGlobalCompactPrinciplesCompliancePolicy: DataPoint<YesNo>?,

    val oecdGuidelinesForMultinationalEnterprisesPolicy: DataPoint<YesNo>?,

    val averageGrossHourlyEarningsMaleEmployees: DataPoint<BigDecimal>?,

    val averageGrossHourlyEarningsFemaleEmployees: DataPoint<BigDecimal>?,

    val femaleBoardMembers: DataPoint<BigDecimal>?,

    val maleBoardMembers: DataPoint<BigDecimal>?,

    val controversialWeaponsExposure: DataPoint<YesNo>?,

    val inorganicPollutants: DataPoint<BigDecimal>?,

    val airPollutants: DataPoint<BigDecimal>?,

    val ozoneDepletionSubstances: DataPoint<BigDecimal>?,

    val carbonReductionInitiatives: DataPoint<YesNo>?,

    val nonRenewableEnergyConsumptionFossilFuels: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionCrudeOil: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionNaturalGas: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionLignite: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionCoal: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionNuclearEnergy: DataPoint<BigDecimal>?,

    val nonRenewableEnergyConsumptionOther: DataPoint<BigDecimal>?,

    val waterConsumption: DataPoint<BigDecimal>?,

    val waterReused: DataPoint<BigDecimal>?,

    val waterManagementPolicy: DataPoint<YesNo>?,

    val waterStressAreaExposure: DataPoint<YesNo>?,

    val manufactureOfAgrochemicalPesticidesProducts: DataPoint<YesNo>?,

    val landDegradationDesertificationSoilSealingExposure: DataPoint<YesNo>?,

    val sustainableAgriculturePolicy: DataPoint<YesNo>?,

    val sustainableOceansAndSeasPolicy: DataPoint<YesNo>?,

    val wasteNonRecycled: DataPoint<BigDecimal>?,

    val threatenedSpeciesExposure: DataPoint<YesNo>?,

    val biodiversityProtectionPolicy: DataPoint<YesNo>?,

    val deforestationPolicy: DataPoint<YesNo>?,

    val securitiesNotCertifiedAsGreen: DataPoint<YesNo>?,

    val workplaceAccidentPreventionPolicy: DataPoint<YesNo>?,

    val rateOfAccidents: DataPoint<BigDecimal>?,

    val workdaysLost: DataPoint<BigDecimal>?,

    val supplierCodeOfConduct: DataPoint<YesNo>?,

    val grievanceHandlingMechanism: DataPoint<YesNo>?,

    val whistleblowerProtectionPolicy: DataPoint<YesNo>?,

    val reportedIncidentsOfDiscrimination: DataPoint<BigDecimal>?,

    val sanctionsIncidentsOfDiscrimination: DataPoint<BigDecimal>?,

    val ceoToEmployeePayGap: DataPoint<BigDecimal>?,

    val humanRightsDueDiligence: DataPoint<YesNo>?,

    val traffickingInHumanBeingsPolicy: DataPoint<YesNo>?,

    val reportedChildLabourIncidents: DataPoint<YesNo>?,

    val reportedForcedOrCompulsoryLabourIncidents: DataPoint<YesNo>?,

    val reportedIncidentsOfHumanRights: DataPoint<BigDecimal>?,

    val reportedCasesOfBriberyCorruption: DataPoint<YesNo>?,

    val reportedConvictionsOfBriberyCorruption: DataPoint<BigDecimal>?,

    val reportedFinesOfBriberyCorruption: DataPoint<BigDecimal>?,

    val referencedReports: Map<String, CompanyReport>? = null,
)
