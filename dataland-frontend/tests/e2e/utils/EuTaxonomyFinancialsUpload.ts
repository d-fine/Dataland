import {
  EuTaxonomyDataForFinancials,
  EligibilityKpis,
  DataPointBigDecimal,
  EuTaxonomyDataForFinancialsControllerApi,
  Configuration,
  DataMetaInformation,
  DataTypeEnum,
} from "@clients/backend";
import { FixtureData } from "@sharedUtils/Fixtures";
import Chainable = Cypress.Chainable;

/**
 * Submits the eutaxonomy-financials upload form and checks that the upload completes successfully
 *
 * @returns the resulting cypress chainable
 */
export function submitEuTaxonomyFinancialsUploadForm(): Cypress.Chainable {
  cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyFinancials}`).as("postCompanyAssociatedData");
  cy.get('button[data-test="submitButton"]').click();
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
export function fillEuTaxonomyForFinancialsUploadForm(data: EuTaxonomyDataForFinancials): void {
  cy.get('[data-test="MultiSelectfinancialServicesTypes"]')
    .click()
    .get("div.p-multiselect-panel")
    .find("li.p-multiselect-item")
    .each(($el) => {
      cy.wrap($el).click({ force: true });
    });
  cy.get('[data-test="addKpisButton"]').click({ force: true });
  if (data.reportingObligation !== undefined) {
    cy.get(`input[name="reportingObligation"][value=${data.reportingObligation.toString()}]`).check();
  }
  cy.get(
    `input[name="fiscalYearDeviation"][value=${
      data.fiscalYearDeviation ? data.fiscalYearDeviation.toString() : "Deviation"
    }]`
  ).check();
  cy.get('input[name="fiscalYearEnd"]').type(`${data.fiscalYearEnd ? data.fiscalYearEnd.toString() : "2022-03-03"}`, {
    force: true,
  });
  cy.get('input[name="scopeOfEntities"][value="No"]').check();
  cy.get('div[id="jumpLinks"] li:last').click();
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
  if (value !== undefined && value.value !== undefined) {
    const eligibleRevenue = value.value.toString();
    if (divTag === "") {
      cy.get(`[data-test="${inputsTag}"]`).find('input[name="value"]').type(eligibleRevenue);
      cy.get(`[data-test="${inputsTag}"]`).find('input[name="page"]').type(eligibleRevenue);
      cy.get(`[data-test="${inputsTag}"]`).find('select[name="report"]').select(1);
      cy.get(`[data-test="${inputsTag}"]`).find('select[name="quality"]').select(1);
      cy.get(`[data-test="${inputsTag}"]`)
        .find('textarea[name="comment"]')
        .type(`${value.comment ?? "comment"}`);
    } else {
      cy.get(`[data-test="${divTag}"]`)
        .find(`[data-test="${inputsTag}"]`)
        .find('input[name="value"]')
        .type(eligibleRevenue);
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
