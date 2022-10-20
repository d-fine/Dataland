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
    val dataDate: LocalDate? = null,

    val lksgInScope: YesNo? = null,

    val companyLegalForm: String? = null,

    val vatIdentificationNumber: String? = null,

    val numberOfEmployees: BigDecimal? = null,

    val shareOfTemporaryWorkers: BigDecimal? = null,

    val totalRevenue: BigDecimal? = null,

    val totalRevenueCurrency: String? = null,

    val responsibilitiesForFairWorkingConditions: YesNo? = null,

    val responsibilitiesForTheEnvironment: YesNo? = null,

    val responsibilitiesForOccupationalSafety: YesNo? = null,

    val riskManagementSystem: YesNo? = null,

    val grievanceHandlingMechanism: YesNo? = null,

    val grievanceHandlingMechanismUsedForReporting: YesNo? = null,

    val codeOfConduct: YesNo? = null,

    val codeOfConductRiskManagementTopics: YesNo? = null,

    val codeOfConductTraining: YesNo? = null,

    val legalProceedings: YesNo? = null,

    val employeeUnder18: YesNo? = null,

    val employeeUnder18Under15: YesNo? = null,

    val employeeUnder18Apprentices: YesNo? = null,

    val employmentUnderLocalMinimumAgePrevention: YesNo? = null,

    val employmentUnderLocalMinimumAgePreventionEmploymentContracts: YesNo? = null,

    val employmentUnderLocalMinimumAgePreventionJobDescription: YesNo? = null,

    val employmentUnderLocalMinimumAgePreventionIdentityDocuments: YesNo? = null,

    val employmentUnderLocalMinimumAgePreventionTraining: YesNo? = null,

    val employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: YesNo? = null,

    val forcedLabourAndSlaveryPrevention: YesNo? = null,

    val forcedLabourAndSlaveryPreventionEmploymentContracts: YesNo? = null,

    val forcedLabourAndSlaveryPreventionIdentityDocuments: YesNo? = null,

    val forcedLabourAndSlaveryPreventionFreeMovement: YesNo? = null,

    val forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets: YesNo? = null,

    val forcedLabourAndSlaveryPreventionProvisionTraining: YesNo? = null,

    val documentedWorkingHoursAndWages: YesNo? = null,

    val adequateLivingWage: YesNo? = null,

    val regularWagesProcessFlow: YesNo? = null,

    val fixedHourlyWages: YesNo? = null,

    val oshMonitoring: YesNo? = null,

    val oshPolicy: YesNo? = null,

    val oshPolicyPersonalProtectiveEquipment: YesNo? = null,

    val oshPolicyMachineSafety: YesNo? = null,

    val oshPolicyDisasterBehaviouralResponse: YesNo? = null,

    val oshPolicyAccidentsBehaviouralResponse: YesNo? = null,

    val oshPolicyWorkplaceErgonomics: YesNo? = null,

    val oshPolicyHandlingChemicalsAndOtherHazardousSubstances: YesNo? = null,

    val oshPolicyFireProtection: YesNo? = null,

    val oshPolicyWorkingHours: YesNo? = null,

    val oshPolicyTrainingAddressed: YesNo? = null,

    val oshPolicyTraining: YesNo? = null,

    val oshManagementSystem: YesNo? = null,

    val oshManagementSystemInternationalCertification: YesNo? = null,

    val oshManagementSystemNationalCertification: YesNo? = null,

    val workplaceAccidentsUnder10: YesNo? = null,

    val oshTraining: YesNo? = null,

    val freedomOfAssociation: YesNo? = null,

    val discriminationForTradeUnionMembers: YesNo? = null,

    val freedomOfOperationForTradeUnion: YesNo? = null,

    val freedomOfAssociationTraining: YesNo? = null,

    val worksCouncil: YesNo? = null,

    val diversityAndInclusionRole: YesNo? = null,

    val preventionOfMistreatments: YesNo? = null,

    val equalOpportunitiesOfficer: YesNo? = null,

    val riskOfHarmfulPollution: YesNo? = null,

    val unlawfulEvictionAndTakingOfLand: YesNo? = null,

    val useOfPrivatePublicSecurityForces: YesNo? = null,

    val useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: YesNo? = null,

    val mercuryAndMercuryWasteHandling: YesNo? = null,

    val mercuryAndMercuryWasteHandlingPolicy: YesNo? = null,

    val chemicalHandling: YesNo? = null,

    val environmentalManagementSystem: YesNo? = null,

    val environmentalManagementSystemInternationalCertification: YesNo? = null,

    val environmentalManagementSystemNationalCertification: YesNo? = null,

    val legalRestrictedWaste: YesNo? = null,

    val legalRestrictedWasteProcesses: YesNo? = null,

    val mercuryAddedProductsHandling: YesNo? = null,

    val mercuryAddedProductsHandlingRiskOfExposure: YesNo? = null,

    val mercuryAddedProductsHandlingRiskOfDisposal: YesNo? = null,

    val mercuryAndMercuryCompoundsProductionAndUse: YesNo? = null,

    val mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: YesNo? = null,

    val persistentOrganicPollutantsProductionAndUse: YesNo? = null,

    val persistentOrganicPollutantsProductionAndUseRiskOfExposure: YesNo? = null,

    val persistentOrganicPollutantsProductionAndUseRiskOfDisposal: YesNo? = null,

    val persistentOrganicPollutantsProductionAndUseTransboundaryMovements: YesNo? = null,

    val persistentOrganicPollutantsProductionAndUseRiskForImportingState: YesNo? = null,

    val hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein: YesNo? = null,

    val hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein: YesNo? = null,

    val hazardousWasteDisposal: YesNo? = null,

    val hazardousWasteDisposalRiskOfImport: YesNo? = null,

    val hazardousAndOtherWasteImport: YesNo? = null,

    val iso26000: YesNo? = null,

    val sa8000Certification: YesNo? = null,

    val smetaSocialAuditConcept: YesNo? = null,

    val betterWorkProgramCertificate: YesNo? = null,

    val iso45001Certification: YesNo? = null,

    val iso14000Certification: YesNo? = null,

    val emasCertification: YesNo? = null,

    val iso37001Certification: YesNo? = null,

    val iso37301Certification: YesNo? = null,

    val riskManagementSystemCertification: YesNo? = null,

    val amforiBsciAuditReport: YesNo? = null,

    val initiativeClauseSocialCertification: YesNo? = null,

    val responsibleBusinessAssociationCertification: YesNo? = null,

    val fairLabourAssociationCertification: YesNo? = null,

    val listOfProductionSites: List<ProductionSite>? = null,
)
