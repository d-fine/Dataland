import {
  Configuration,
  LksgData,
  LksgDataControllerApi,
  DataMetaInformation,
  CompanyInformation,
  InHouseProductionOrContractProcessing,
} from "@clients/backend";
import { UploadIds } from "./GeneralApiUtils";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "./CompanyUpload";
/**
 * Uploads a single LKSG data entry for a company
 *
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param reportingPeriod The reporting period to use for the upload
 * @param data The Dataset to upload
 */
export async function uploadOneLksgDatasetViaApi(
  token: string,
  companyId: string,
  reportingPeriod: string,
  data: LksgData
): Promise<DataMetaInformation> {
  const response = await new LksgDataControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedLksgData({
    companyId,
    reportingPeriod,
    data,
  });
  return response.data;
}

/**
 * Uploads a company and single LkSG data entry for a company
 *
 * @param token The API bearer token to use
 * @param companyInformation The company information to use for the company upload
 * @param testData The Dataset to upload
 * @param reportingPeriod The reporting period to use for the upload
 * @returns an object which contains the companyId from the company upload and the dataId from the data upload
 */
export function uploadCompanyAndLksgDataViaApi(
  token: string,
  companyInformation: CompanyInformation,
  testData: LksgData,
  reportingPeriod: string
): Promise<UploadIds> {
  return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
    (storedCompany) => {
      return uploadOneLksgDatasetViaApi(token, storedCompany.companyId, reportingPeriod, testData).then(
        (dataMetaInformation) => {
          return { companyId: storedCompany.companyId, dataId: dataMetaInformation.dataId };
        }
      );
    }
  );
}

/**
 * Uploads a single LKSG data entry for a company via form
 */
export function uploadLksgDataViaForm(): void {
  Cypress.Keyboard.defaults({
    keystrokeDelay: 0,
  });
  const yesNoInputs = [
    "employeeUnder18",
    "employeeUnder15",
    "employeeUnder18Apprentices",
    "employmentUnderLocalMinimumAgePrevention",
    "employmentUnderLocalMinimumAgePreventionEmploymentContracts",
    "employmentUnderLocalMinimumAgePreventionJobDescription",
    "employmentUnderLocalMinimumAgePreventionIdentityDocuments",
    "employmentUnderLocalMinimumAgePreventionTraining",
    "employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge",
    "forcedLabourAndSlaveryPrevention",
    "forcedLabourAndSlaveryPreventionEmploymentContracts",
    "forcedLabourAndSlaveryPreventionIdentityDocuments",
    "forcedLabourAndSlaveryPreventionFreeMovement",
    "forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets",
    "iso26000",
    "sa8000Certification",
    "smetaSocialAuditConcept",
    "betterWorkProgramCertificate",
    "grievanceHandlingMechanism",
    "grievanceHandlingMechanismUsedForReporting",
    "oshMonitoring",
    "oshPolicy",
    "oshPolicyPersonalProtectiveEquipment",
    "oshPolicyMachineSafety",
    "oshManagementSystem",
    "freedomOfAssociation",
    "freedomOfOperationForTradeUnion",
    "diversityAndInclusionRole",
    "preventionOfMistreatments",
    "unlawfulEvictionAndTakingOfLand",
    "responsibilitiesForFairWorkingConditions",
    "responsibilitiesForTheEnvironment",
    "responsibilitiesForOccupationalSafety",
    "riskManagementSystem",
    "codeOfConduct",
    "mercuryAndMercuryWasteHandling",
    "mercuryAddedProductsHandlingRiskOfDisposal",
    "hazardousAndOtherWasteImport",
  ];

  cy.get('[data-test="lksgDataDate"]').click();
  cy.get("div.p-datepicker").find('button[aria-label="Previous Month"]').click();
  cy.get("div.p-datepicker").find('span:contains("13")').click();
  cy.get('input[name="dataDate"]').should(($input) => {
    const val = $input.val();
    expect(val).to.include("-13");
  });
  cy.get('div[data-test="lksgInScope"]').find('input[value="Yes"]').click().should("be.checked");
  cy.get("input[name=vatIdentificationNumber]").type("BE0999999999");
  cy.get("input[name=numberOfEmployees]").type("7999");
  cy.get("input[name=shareOfTemporaryWorkers]").type("29.23");
  cy.get("input[name=totalRevenue]").type("10043000");
  cy.get("input[name=totalRevenueCurrency]").type("ISO4217ISO4217");
  cy.get('div[data-test="IsYourCompanyManufacturingCompany"]').find('input[value="Yes"]').click().should("be.checked");
  cy.get('div[data-test="productionSiteSection"]').should("be.visible");
  cy.get('button[data-test="ADD-NEW-Production-Site-button"]').should("be.visible").click();
  cy.get('div[data-test="productionSiteSection"]').should("have.length", 2);
  cy.get('[data-test="removeItemFromlistOfProductionSites"]').eq(1).click();
  cy.get('div[data-test="productionSiteSection"]').should("have.length", 1);
  cy.get('div[data-test="productionSiteSection"] input[name="name"]').type("CCddEE");
  cy.get('div[data-test="isInHouseProductionOrIsContractProcessing"]')
    .find(`input[value="${InHouseProductionOrContractProcessing.InHouseProduction}"]`)
    .click()
    .should("be.checked");
  cy.get('div[data-test="productionSiteSection"] input[name="streetAndHouseNumber"]').type("Live-street 28");
  cy.get('div[data-test="productionSiteSection"] select[name="country"]').select("Belgium");
  cy.get('div[data-test="productionSiteSection"] input[name="city"]').type("Capitol City");
  cy.get('div[data-test="productionSiteSection"] input[name="postalCode"]').type("WE-3133");
  cy.get('input[data-test="listOfGoodsOrServices"]').type("1;2;3");
  cy.get('div[data-test="productionSiteSection"] button[aria-label="Add"]').click();
  cy.get('div[data-test="productionSiteSection"] span.form-list-item').its("length").should("eq", 3);

  yesNoInputs.forEach((name) => {
    cy.get(`input[name=${name}][value="Yes"]`).click().should("be.checked");
  });
  cy.get('button[data-test="submitButton"]').click();
  cy.get("div.p-message-success").should("be.visible");
}
///
