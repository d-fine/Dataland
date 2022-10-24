package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.LksgDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataLksgData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Lksg {
    private val tokenHandler = TokenHandler()
    private val lksgDataControllerApi =
        LksgDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private val testDataProviderForLksgData =
        TestDataProvider(LksgData::class.java)

    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private fun postOneCompanyAndLksg():
        Pair<DataMetaInformation, LksgData> {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val testData = testDataProviderForLksgData.getTData(1).first()
        val receivedCompanyId = companyDataControllerApi.postCompany(
            testDataProviderForLksgData.getCompanyInformationWithoutIdentifiers(1).first()
        ).companyId
        val receivedDataMetaInformation = lksgDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataLksgData(receivedCompanyId, testData)
        )
        return Pair(
            DataMetaInformation(
                companyId = receivedCompanyId,
                dataId = receivedDataMetaInformation.dataId,
                dataType = receivedDataMetaInformation.dataType
            ),
            testData
        )
    }

    @Test
    fun `post a company with Lksg data and check if the data can be retrieved correctly`() {
        val (receivedDataMetaInformation, uploadedData) = postOneCompanyAndLksg()
        val downloadedAssociatedData = lksgDataControllerApi
            .getCompanyAssociatedData(receivedDataMetaInformation.dataId)
        val downloadedAssociatedDataType = metaDataControllerApi.getDataMetaInfo(receivedDataMetaInformation.dataId)

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType.dataType)
        Assertions.assertEquals(uploadedData, downloadedAssociatedData.data)
    }

    @Test
    @Suppress("LongMethod")
    fun `post a company without Lksg data and check if it works`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Admin)
        val receivedCompanyId = companyDataControllerApi.postCompany(
            testDataProviderForLksgData.getCompanyInformationWithoutIdentifiers(1).first()
        ).companyId
        val nullData = LksgData(
            dataDate = null,
            lksgInScope = null,
            companyLegalForm = null,
            vatIdentificationNumber = null,
            numberOfEmployees = null,
            shareOfTemporaryWorkers = null,
            totalRevenue = null,
            totalRevenueCurrency = null,
            responsibilitiesForFairWorkingConditions = null,
            responsibilitiesForTheEnvironment = null,
            responsibilitiesForOccupationalSafety = null,
            riskManagementSystem = null,
            grievanceHandlingMechanism = null,
            grievanceHandlingMechanismUsedForReporting = null,
            codeOfConduct = null,
            codeOfConductRiskManagementTopics = null,
            codeOfConductTraining = null,
            legalProceedings = null,
            employeeUnder18 = null,
            employeeUnder18Under15 = null,
            employeeUnder18Apprentices = null,
            employmentUnderLocalMinimumAgePrevention = null,
            employmentUnderLocalMinimumAgePreventionEmploymentContracts = null,
            employmentUnderLocalMinimumAgePreventionJobDescription = null,
            employmentUnderLocalMinimumAgePreventionIdentityDocuments = null,
            employmentUnderLocalMinimumAgePreventionTraining = null,
            employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge = null,
            forcedLabourAndSlaveryPrevention = null,
            forcedLabourAndSlaveryPreventionEmploymentContracts = null,
            forcedLabourAndSlaveryPreventionIdentityDocuments = null,
            forcedLabourAndSlaveryPreventionFreeMovement = null,
            forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets = null,
            forcedLabourAndSlaveryPreventionProvisionTraining = null,
            documentedWorkingHoursAndWages = null,
            adequateLivingWage = null,
            regularWagesProcessFlow = null,
            fixedHourlyWages = null,
            oshMonitoring = null,
            oshPolicy = null,
            oshPolicyPersonalProtectiveEquipment = null,
            oshPolicyMachineSafety = null,
            oshPolicyDisasterBehaviouralResponse = null,
            oshPolicyAccidentsBehaviouralResponse = null,
            oshPolicyWorkplaceErgonomics = null,
            oshPolicyHandlingChemicalsAndOtherHazardousSubstances = null,
            oshPolicyFireProtection = null,
            oshPolicyWorkingHours = null,
            oshPolicyTrainingAddressed = null,
            oshPolicyTraining = null,
            oshManagementSystem = null,
            oshManagementSystemInternationalCertification = null,
            oshManagementSystemNationalCertification = null,
            workplaceAccidentsUnder10 = null,
            oshTraining = null,
            freedomOfAssociation = null,
            discriminationForTradeUnionMembers = null,
            freedomOfOperationForTradeUnion = null,
            freedomOfAssociationTraining = null,
            worksCouncil = null,
            diversityAndInclusionRole = null,
            preventionOfMistreatments = null,
            equalOpportunitiesOfficer = null,
            riskOfHarmfulPollution = null,
            unlawfulEvictionAndTakingOfLand = null,
            useOfPrivatePublicSecurityForces = null,
            useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights = null,
            mercuryAndMercuryWasteHandling = null,
            mercuryAndMercuryWasteHandlingPolicy = null,
            chemicalHandling = null,
            environmentalManagementSystem = null,
            environmentalManagementSystemInternationalCertification = null,
            environmentalManagementSystemNationalCertification = null,
            legalRestrictedWaste = null,
            legalRestrictedWasteProcesses = null,
            mercuryAddedProductsHandling = null,
            mercuryAddedProductsHandlingRiskOfExposure = null,
            mercuryAddedProductsHandlingRiskOfDisposal = null,
            mercuryAndMercuryCompoundsProductionAndUse = null,
            mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure = null,
            persistentOrganicPollutantsProductionAndUse = null,
            persistentOrganicPollutantsProductionAndUseRiskOfExposure = null,
            persistentOrganicPollutantsProductionAndUseRiskOfDisposal = null,
            persistentOrganicPollutantsProductionAndUseTransboundaryMovements = null,
            persistentOrganicPollutantsProductionAndUseRiskForImportingState = null,
            hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein = null,
            hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein = null,
            hazardousWasteDisposal = null,
            hazardousWasteDisposalRiskOfImport = null,
            hazardousAndOtherWasteImport = null,
            iso26000 = null,
            sa8000Certification = null,
            smetaSocialAuditConcept = null,
            betterWorkProgramCertificate = null,
            iso45001Certification = null,
            iso14000Certification = null,
            emasCertification = null,
            iso37001Certification = null,
            iso37301Certification = null,
            riskManagementSystemCertification = null,
            amforiBsciAuditReport = null,
            initiativeClauseSocialCertification = null,
            responsibleBusinessAssociationCertification = null,
            fairLabourAssociationCertification = null,
            listOfProductionSites = null,
        )
        val receivedDataMetaInformation = lksgDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataLksgData(receivedCompanyId, nullData)
        )
        val downloadedAssociatedData = lksgDataControllerApi
            .getCompanyAssociatedData(receivedDataMetaInformation.dataId)
        val downloadedAssociatedDataType = metaDataControllerApi.getDataMetaInfo(receivedDataMetaInformation.dataId)

        Assertions.assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        Assertions.assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType.dataType)
        Assertions.assertEquals(nullData, downloadedAssociatedData.data)
    }
}
