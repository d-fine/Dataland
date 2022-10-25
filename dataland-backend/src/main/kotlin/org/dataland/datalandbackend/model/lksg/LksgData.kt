package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the questionnaire for the LKSG framework
 */
@DataType("lksg")
data class LksgData(
    val dataDate: LocalDate?,

    val lksgInScope: YesNo?,

    val companyLegalForm: String?,

    val vatIdentificationNumber: String?,

    val numberOfEmployees: BigDecimal?,

    val shareOfTemporaryWorkers: BigDecimal?,

    val totalRevenue: BigDecimal?,

    val totalRevenueCurrency: String?,

    val responsibilitiesForFairWorkingConditions: YesNo?,

    val responsibilitiesForTheEnvironment: YesNo?,

    val responsibilitiesForOccupationalSafety: YesNo?,

    val riskManagementSystem: YesNo?,

    val grievanceHandlingMechanism: YesNo?,

    val grievanceHandlingMechanismUsedForReporting: YesNo?,

    val codeOfConduct: YesNo?,

    val codeOfConductRiskManagementTopics: YesNo?,

    val codeOfConductTraining: YesNo?,

    val legalProceedings: YesNo?,

    val employeeUnder18: YesNo?,

    val employeeUnder18Under15: YesNo?,

    val employeeUnder18Apprentices: YesNo?,

    val employmentUnderLocalMinimumAgePrevention: YesNo?,

    val employmentUnderLocalMinimumAgePreventionEmploymentContracts: YesNo?,

    val employmentUnderLocalMinimumAgePreventionJobDescription: YesNo?,

    val employmentUnderLocalMinimumAgePreventionIdentityDocuments: YesNo?,

    val employmentUnderLocalMinimumAgePreventionTraining: YesNo?,

    val employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: YesNo?,

    val forcedLabourAndSlaveryPrevention: YesNo?,

    val forcedLabourAndSlaveryPreventionEmploymentContracts: YesNo?,

    val forcedLabourAndSlaveryPreventionIdentityDocuments: YesNo?,

    val forcedLabourAndSlaveryPreventionFreeMovement: YesNo?,

    val forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets: YesNo?,

    val forcedLabourAndSlaveryPreventionProvisionTraining: YesNo?,

    val documentedWorkingHoursAndWages: YesNo?,

    val adequateLivingWage: YesNo?,

    val regularWagesProcessFlow: YesNo?,

    val fixedHourlyWages: YesNo?,

    val oshMonitoring: YesNo?,

    val oshPolicy: YesNo?,

    val oshPolicyPersonalProtectiveEquipment: YesNo?,

    val oshPolicyMachineSafety: YesNo?,

    val oshPolicyDisasterBehaviouralResponse: YesNo?,

    val oshPolicyAccidentsBehaviouralResponse: YesNo?,

    val oshPolicyWorkplaceErgonomics: YesNo?,

    val oshPolicyHandlingChemicalsAndOtherHazardousSubstances: YesNo?,

    val oshPolicyFireProtection: YesNo?,

    val oshPolicyWorkingHours: YesNo?,

    val oshPolicyTrainingAddressed: YesNo?,

    val oshPolicyTraining: YesNo?,

    val oshManagementSystem: YesNo?,

    val oshManagementSystemInternationalCertification: YesNo?,

    val oshManagementSystemNationalCertification: YesNo?,

    val workplaceAccidentsUnder10: YesNo?,

    val oshTraining: YesNo?,

    val freedomOfAssociation: YesNo?,

    val discriminationForTradeUnionMembers: YesNo?,

    val freedomOfOperationForTradeUnion: YesNo?,

    val freedomOfAssociationTraining: YesNo?,

    val worksCouncil: YesNo?,

    val diversityAndInclusionRole: YesNo?,

    val preventionOfMistreatments: YesNo?,

    val equalOpportunitiesOfficer: YesNo?,

    val riskOfHarmfulPollution: YesNo?,

    val unlawfulEvictionAndTakingOfLand: YesNo?,

    val useOfPrivatePublicSecurityForces: YesNo?,

    val useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: YesNo?,

    val mercuryAndMercuryWasteHandling: YesNo?,

    val mercuryAndMercuryWasteHandlingPolicy: YesNo?,

    val chemicalHandling: YesNo?,

    val environmentalManagementSystem: YesNo?,

    val environmentalManagementSystemInternationalCertification: YesNo?,

    val environmentalManagementSystemNationalCertification: YesNo?,

    val legalRestrictedWaste: YesNo?,

    val legalRestrictedWasteProcesses: YesNo?,

    val mercuryAddedProductsHandling: YesNo?,

    val mercuryAddedProductsHandlingRiskOfExposure: YesNo?,

    val mercuryAddedProductsHandlingRiskOfDisposal: YesNo?,

    val mercuryAndMercuryCompoundsProductionAndUse: YesNo?,

    val mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: YesNo?,

    val persistentOrganicPollutantsProductionAndUse: YesNo?,

    val persistentOrganicPollutantsProductionAndUseRiskOfExposure: YesNo?,

    val persistentOrganicPollutantsProductionAndUseRiskOfDisposal: YesNo?,

    val persistentOrganicPollutantsProductionAndUseTransboundaryMovements: YesNo?,

    val persistentOrganicPollutantsProductionAndUseRiskForImportingState: YesNo?,

    val hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein: YesNo?,

    val hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein: YesNo?,

    val hazardousWasteDisposal: YesNo?,

    val hazardousWasteDisposalRiskOfImport: YesNo?,

    val hazardousAndOtherWasteImport: YesNo?,

    val iso26000: YesNo?,

    val sa8000Certification: YesNo?,

    val smetaSocialAuditConcept: YesNo?,

    val betterWorkProgramCertificate: YesNo?,

    val iso45001Certification: YesNo?,

    val iso14000Certification: YesNo?,

    val emasCertification: YesNo?,

    val iso37001Certification: YesNo?,

    val iso37301Certification: YesNo?,

    val riskManagementSystemCertification: YesNo?,

    val amforiBsciAuditReport: YesNo?,

    val initiativeClauseSocialCertification: YesNo?,

    val responsibleBusinessAssociationCertification: YesNo?,

    val fairLabourAssociationCertification: YesNo?,

    val listOfProductionSites: List<ProductionSite>?,
)
