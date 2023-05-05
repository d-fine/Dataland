import {
  CompanyAssociatedDataEuTaxonomyDataForFinancials,
  CompanyInformation,
  Configuration,
  DataMetaInformation,
  DataPointBigDecimal,
  DataTypeEnum,
  EligibilityKpis,
  EuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsControllerApi,
} from "@clients/backend";
import { FixtureData } from "@sharedUtils/Fixtures";
import Chainable = Cypress.Chainable;
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { dateFormElement } from "@sharedUtils/components/DateFormElement";
import { gotoEditFormOfMostRecentDataset } from "@e2e/utils/GeneralApiUtils";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { TEST_PDF_FILE_NAME } from "@e2e/utils/Constants";
import { CyHttpMessages } from "cypress/types/net-stubbing";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { submitFilledInEuTaxonomyForm } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";

/**
 * Submits the eutaxonomy-financials upload form and checks that the upload completes successfully
 *
 * @returns the resulting cypress chainable
 */
export function submitEuTaxonomyFinancialsUploadForm(): Cypress.Chainable {
  cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyFinancials}`).as("postCompanyAssociatedData");
  submitButton.clickButton();
  cy.on("uncaught:exception", (err) => {
    expect(err.message).to.include("unhandled promise rejection");
    return false;
  });
  return cy.wait("@postCompanyAssociatedData").then((interception) => {
    expect(interception.response?.statusCode).to.eq(200);
  });
}

/**
 * Fills the eutaxonomy-financials upload form with the given dataset
 *
 * @param data the data to fill the form with
 */
export function fillAndValidateEuTaxonomyForFinancialsUploadForm(data: EuTaxonomyDataForFinancials): void {
  dateFormElement.selectDayOfNextMonth("fiscalYearEnd", 12);
  dateFormElement.validateDay("fiscalYearEnd", 12);

  cy.get('[data-test="MultiSelectfinancialServicesTypes"]')
    .click()
    .get("div.p-multiselect-panel")
    .find("li.p-multiselect-item")
    .first()
    .click();
  cy.get('[data-test="addKpisButton"]').click({ force: true });
  cy.get('[data-test="removeSectionButton"]').click({ force: true });

  cy.get('[data-test="MultiSelectfinancialServicesTypes"]')
    .click()
    .get("div.p-multiselect-panel")
    .find("li.p-multiselect-item")
    .each(($el) => {
      cy.wrap($el).click({ force: true });
    });
  cy.get('[data-test="addKpisButton"]').click({ force: true });
  cy.get('[data-test="dataPointToggle"]')
    .eq(1)
    .should("exist")
    .should("contain.text", "Data point is available")
    .find('[data-test="dataPointToggleButton"]')
    .click();
  cy.get('[data-test="dataPointToggle"]')
    .eq(1)
    .find('[data-test="dataPointToggleTitle"]')
    .should("contain.text", "Data point is not available");
  cy.get('[data-test="dataPointToggle"]').eq(1).find('[data-test="dataPointToggleButton"]').click();
  cy.get('[data-test="dataPointToggle"]')
    .eq(1)
    .find('[data-test="dataPointToggleTitle"]')
    .should("contain.text", "Data point is available");
  if (data.reportingObligation !== undefined) {
    cy.get(`input[name="reportingObligation"][value=${data.reportingObligation.toString()}]`).check();
  }
  cy.get(
    `input[name="fiscalYearDeviation"][value=${
      data.fiscalYearDeviation ? data.fiscalYearDeviation.toString() : "Deviation"
    }]`
  ).check();
  cy.get('input[name="scopeOfEntities"][value="No"]').check();
  cy.get('div[data-test="submitSideBar"] li:last a').click();
  cy.window().then((win) => {
    const scrollPosition = win.scrollY;
    expect(scrollPosition).to.be.greaterThan(0);
  });
  cy.get('button[data-test="submitButton"]').click();
  cy.window().then((win) => {
    const scrollPosition = win.scrollY;
    expect(scrollPosition).to.be.greaterThan(0);
  });
  cy.get(
    `input[name="activityLevelReporting"][value=${
      data.activityLevelReporting ? data.activityLevelReporting.toString() : "No"
    }]`
  ).check();

  cy.get('input[name="numberOfEmployees"]').type(
    `${data.numberOfEmployees ? data.numberOfEmployees.toString() : "13"}`
  );
  cy.get('button[data-test="removeSectionButton"]').should("exist").should("have.class", "ml-auto");

  cy.get('[data-test="assuranceSection"] select[name="assurance"]').select(2);
  cy.get('[data-test="assuranceSection"] input[name="provider"]').type("Assurance Provider", { force: true });
  cy.get('[data-test="assuranceSection"] select[name="report"]').select(1);

  fillEligibilityKpis("creditInstitutionKpis", data.eligibilityKpis?.CreditInstitution);
  fillEligibilityKpis("insuranceKpis", data.eligibilityKpis?.InsuranceOrReinsurance);
  fillEligibilityKpis("investmentFirmKpis", data.eligibilityKpis?.InvestmentFirm);
  fillEligibilityKpis("assetManagementKpis", data.eligibilityKpis?.AssetManagement);
  fillField(
    "insuranceKpis",
    "taxonomyEligibleNonLifeInsuranceActivities",
    data.insuranceKpis?.taxonomyEligibleNonLifeInsuranceActivities
  );
  fillField("investmentFirmKpis", "greenAssetRatio", data.investmentFirmKpis?.greenAssetRatio);
  fillField(
    "creditInstitutionKpis",
    "tradingPortfolioAndInterbankLoans",
    data.creditInstitutionKpis?.tradingPortfolioAndInterbankLoans
  );
  fillField("creditInstitutionKpis", "tradingPortfolio", data.creditInstitutionKpis?.tradingPortfolio);
  fillField("creditInstitutionKpis", "interbankLoans", data.creditInstitutionKpis?.interbankLoans);
  fillField("creditInstitutionKpis", "greenAssetRatio", data.creditInstitutionKpis?.greenAssetRatio);
}

/**
 * Fills a set with eligibility-kpis for different company types
 *
 * @param divTag value of the parent div data-test attribute to fill in
 * @param data the kpi data to use to fill the form
 */
function fillEligibilityKpis(divTag: string, data: EligibilityKpis | undefined): void {
  fillField(divTag, "taxonomyEligibleActivity", data?.taxonomyEligibleActivity);
  fillField(divTag, "taxonomyNonEligibleActivity", data?.taxonomyNonEligibleActivity);
  fillField(divTag, "derivatives", data?.derivatives);
  fillField(divTag, "banksAndIssuers", data?.banksAndIssuers);
  fillField(divTag, "investmentNonNfrd", data?.investmentNonNfrd);
}

/**
 * Enters a single decimal inputs field value in the upload eutaxonomy-financials form
 *
 * @param divTag value of the parent div data-test attribute to fill in
 * @param inputsTag value of the parent div data-test attribute to fill in
 * @param value the value to fill in
 */
function fillField(divTag: string, inputsTag: string, value?: DataPointBigDecimal): void {
  if (value?.value) {
    const valueAsString = value.value.toString();
    if (divTag === "") {
      cy.get(`[data-test="${inputsTag}"]`).find('input[name="value"]').type(valueAsString);
      cy.get(`[data-test="${inputsTag}"]`).find('input[name="page"]').type("13");
      cy.get(`[data-test="${inputsTag}"]`).find('select[name="report"]').select(1);
      cy.get(`[data-test="${inputsTag}"]`).find('select[name="quality"]').select(1);
      cy.get(`[data-test="${inputsTag}"]`)
        .find('textarea[name="comment"]')
        .type(`${value.comment ?? "comment"}`);
    } else {
      cy.get(`[data-test="${divTag}"]`)
        .find(`[data-test="${inputsTag}"]`)
        .find('input[name="value"]')
        .type(valueAsString);
      cy.get(`[data-test="${divTag}"]`)
        .find(`[data-test="${inputsTag}"]`)
        .find('input[name="page"]')
        .type(`${value.dataSource?.page ?? "13"}`);
      cy.get(`[data-test="${divTag}"]`).find(`[data-test="${inputsTag}"]`).find('select[name="report"]').select(1);
      cy.get(`[data-test="${divTag}"]`).find(`[data-test="${inputsTag}"]`).find('select[name="quality"]').select(1);
      cy.get(`[data-test="${divTag}"]`)
        .find(`[data-test="${inputsTag}"]`)
        .find('textarea[name="comment"]')
        .type(`${value.comment ?? "comment"}`);
    }
  }
}

/**
 * Extracts the first eutaxonomy-financials dataset from the fake fixtures
 *
 * @returns the first eutaxonomy-financials dataset from the fake fixtures
 */
export function getFirstEuTaxonomyFinancialsFixtureDataFromFixtures(): Chainable<
  FixtureData<EuTaxonomyDataForFinancials>
> {
  return cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (jsonContent) {
    const companiesWithEuTaxonomyDataForFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
    return companiesWithEuTaxonomyDataForFinancials[0];
  });
}

/**
 * Uploads a single eutaxonomy-financials data entry for a company via the Dataland API
 *
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param reportingPeriod The reporting period to use for the upload
 * @param data The Dataset to upload
 * @returns a promise on the created data meta information
 */
export async function uploadOneEuTaxonomyFinancialsDatasetViaApi(
  token: string,
  companyId: string,
  reportingPeriod: string,
  data: EuTaxonomyDataForFinancials
): Promise<DataMetaInformation> {
  const response = await new EuTaxonomyDataForFinancialsControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedEuTaxonomyDataForFinancials({
    companyId,
    reportingPeriod,
    data,
  });
  return response.data;
}

/**
 * Visits the edit page for the eu taxonomy dataset for financial companies via navigation.
 *
 * @param companyId the id of the company for which to edit a dataset
 * @param expectIncludedFile specifies if the test file is expected to be in the server response
 */
export function gotoEditForm(companyId: string, expectIncludedFile: boolean): void {
  gotoEditFormOfMostRecentDataset(companyId, DataTypeEnum.EutaxonomyFinancials).then((interception) => {
    const referencedReports = assertDefined(
      (interception?.response?.body as CompanyAssociatedDataEuTaxonomyDataForFinancials)?.data?.referencedReports
    );
    expect(TEST_PDF_FILE_NAME in referencedReports).to.equal(expectIncludedFile);
    expect(`${TEST_PDF_FILE_NAME}2` in referencedReports).to.equal(true);
  });
}

/**
 * Uploads a company via POST-request, then an EU Taxonomy dataset for financial companies for the uploaded company
 * via the form in the frontend, and then visits the view page where that dataset is displayed
 *
 * @param companyInformation Company information to be used for the company upload
 * @param testData EU Taxonomy dataset for financial companies to be uploaded
 * @param beforeFormFill is performed before filling the fields of the upload form
 * @param afterFormFill is performed after filling the fields of the upload form
 * @param submissionDataIntercept performs checks on the request itself
 * @param afterDatasetSubmission is performed after the data has been submitted
 */
export function uploadCompanyViaApiAndEuTaxonomyDataForFinancialsViaForm(
  companyInformation: CompanyInformation,
  testData: EuTaxonomyDataForFinancials,
  beforeFormFill: () => void,
  afterFormFill: () => void,
  submissionDataIntercept: (request: CyHttpMessages.IncomingHttpRequest) => void,
  afterDatasetSubmission: (companyId: string) => void
): void {
  getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
    return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
      (storedCompany): void => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        cy.visitAndCheckAppMount(
          `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`
        );
        beforeFormFill();
        fillAndValidateEuTaxonomyForFinancialsUploadForm(testData);
        afterFormFill();
        submitFilledInEuTaxonomyForm(submissionDataIntercept);
        afterDatasetSubmission(storedCompany.companyId);
      }
    );
  });
}
