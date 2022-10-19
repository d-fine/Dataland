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
data class LKSGData(
    val dataDate: LocalDate? = null,

    val LKSGinScope: YesNo? = null,

    val companyLegalForm: String? = null,

    val VATidentificationNumber: String? = null,

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

    val OSHmonitoring: YesNo? = null,

    val OSHpolicy: YesNo? = null,

    val OSHpolicyPersonalProtectiveEquipment: YesNo? = null,

    val OSHpolicyMachineSafety: YesNo? = null,

    val OSHpolicyDisasterBehaviouralResponse: YesNo? = null,

    val OSHpolicyAccidentsBehaviouralResponse: YesNo? = null,

    val OSHpolicyWorkplaceErgonomics: YesNo? = null,

    val OSHpolicyHandlingChemicalsAndOtherHazardousSubstances: YesNo? = null,

    val OSHpolicyFireProtection: YesNo? = null,

    val OSHpolicyWorkingHours: YesNo? = null,

    val OSHpolicyTrainingAddressed: YesNo? = null,

    val OSHpolicyTraining: YesNo? = null,

    val OSHmanagementSystem: YesNo? = null,

    val OSHmanagementSystemInternationalCertification: YesNo? = null,

    val OSHmanagementSystemNationalCertification: YesNo? = null,

    val workplaceAccidentsUnder10: YesNo? = null,

    val OSHtraining: YesNo? = null,

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

    val ISO26000: YesNo? = null,

    val SA8000certification: YesNo? = null,

    val SMETAsocialAuditConcept: YesNo? = null,

    val betterWorkProgramCertificate: YesNo? = null,

    val ISO45001certification: YesNo? = null,

    val ISO14000certification: YesNo? = null,

    val EMAScertification: YesNo? = null,

    val ISO37001certification: YesNo? = null,

    val ISO37301certification: YesNo? = null,

    val riskManagementSystemCertification: YesNo? = null,

    val amforiBSCIAuditReport: YesNo? = null,

    val initiativeClauseSocialCertification: YesNo? = null,

    val responsibleBusinessAssociationCertification: YesNo? = null,

    val fairLabourAssociationCertification: YesNo? = null,

    val listOfProductionSites: List<ProductionSite>? = null,
    )
