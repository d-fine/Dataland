import {
  EuTaxonomyDataForFinancials,
  EligibilityKpis,
  DataPointBigDecimal,
  EuTaxonomyDataForFinancialsControllerApi,
  Configuration,
} from "@clients/backend";
import { FixtureData } from "../fixtures/FixtureUtils";
import Chainable = Cypress.Chainable;

export function submitEuTaxonomyFinancialsUploadForm(): Cypress.Chainable {
  cy.intercept("**/api/data/eutaxonomy-financials").as("postCompanyAssociatedData");
  cy.get('button[name="postEUData"]').click();
  return cy.wait("@postCompanyAssociatedData").get("body").should("contain", "success");
}

/**
 * Uploads a single eutaxonomy-financials data entry for a company via the Dataland upload form
 *
 * @param companyId The Id of the company to upload the dataset for
 * @returns the id of the dataset that has been uploaded
 */
export function uploadDummyEuTaxonomyDataForFinancialsViaForm(companyId: string): Cypress.Chainable {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-financials/upload`);
  fillEuTaxonomyFinancialsDummyUploadFields();
  return submitEuTaxonomyFinancialsUploadForm();
}

/**
 * Fills the eutaxonomy-financials upload form with the given dataset
 *
 * @param data the data to fill the form with
 */
export function fillEuTaxonomyForFinancialsUploadForm(data: EuTaxonomyDataForFinancials): void {
  cy.get("select[name=financialServicesTypes]").select(data.financialServicesTypes || []);

  if (data.assurance?.assurance !== undefined) {
    cy.get("select[name=assurance]").select(data.assurance.assurance.toString());
  }

  if (data.reportingObligation !== undefined) {
    cy.get(`input[name="reportingObligation"][value=${data.reportingObligation.toString()}]`).check();
  }

  fillEligibilityKpis("CreditInstitution", data.eligibilityKpis?.CreditInstitution);
  fillEligibilityKpis("InsuranceOrReinsurance", data.eligibilityKpis?.InsuranceOrReinsurance);
  fillEligibilityKpis("AssetManagement", data.eligibilityKpis?.AssetManagement);
  fillEligibilityKpis("InvestmentFirm", data.eligibilityKpis?.InvestmentFirm);
  fillField(
    "",
    "taxonomyEligibleNonLifeInsuranceActivities",
    data.insuranceKpis?.taxonomyEligibleNonLifeInsuranceActivities
  );
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
 * Fills a set of eligibility-kpis for different company types
 *
 * @param divName the name of the parent div of the kpis to fill in
 * @param data the kpi data to use to fill the form
 */
function fillEligibilityKpis(divName: string, data: EligibilityKpis | undefined): void {
  fillField(divName, "taxonomyEligibleActivity", data?.taxonomyEligibleActivity);
  fillField(divName, "taxonomyNonEligibleActivity", data?.taxonomyNonEligibleActivity);
  fillField(divName, "derivatives", data?.derivatives);
  fillField(divName, "banksAndIssuers", data?.banksAndIssuers);
  fillField(divName, "investmentNonNfrd", data?.investmentNonNfrd);
}

/**
 * Enters a single decimal value into an input field in the upload eutaxonomy-financials form
 *
 * @param divName the name of the parent div of the input field
 * @param inputName the name of the input field
 * @param value the value to fill in
 */
function fillField(divName: string, inputName: string, value?: DataPointBigDecimal): void {
  if (value !== undefined && value.value !== undefined) {
    const input = value.value.toString();
    if (divName === "") {
      cy.get(`input[name="${inputName}"]`).type(input);
    } else {
      cy.get(`div[name="${divName}"]`).find(`input[name="${inputName}"]`).type(input);
    }
  }
}

/**
 * Enters the minimum set of dummy values that can be used to submit an eutaxonomy-financials upload form
 */
function fillEuTaxonomyFinancialsDummyUploadFields(): void {
  cy.get("select[name=financialServicesTypes]").select("Credit Institution");
  cy.get("select[name=assurance]").select("Limited Assurance");
  cy.get('input[name="reportingObligation"][value=Yes]').check();
}

/**
 * Extracts the first eutaxonomy-financials dataset from the fake fixtures
 *
 * @returns the first eutaxonomy-financials dataset from the fake fixtures
 */
export function getFirstEuTaxonomyFinancialsDatasetFromFixtures(): Chainable<EuTaxonomyDataForFinancials> {
  return cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (jsonContent) {
    const companiesWithEuTaxonomyDataForFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
    return companiesWithEuTaxonomyDataForFinancials[0].t;
  });
}

/**
 * Uploads a single eutaxonomy-financials data entry for a company via the Dataland API
 *
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param data The Dataset to upload
 */
export async function uploadOneEuTaxonomyFinancialsDatasetViaApi(
  token: string,
  companyId: string,
  data: EuTaxonomyDataForFinancials
): Promise<void> {
  await new EuTaxonomyDataForFinancialsControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedEuTaxonomyDataForFinancials({
    companyId,
    data,
  });
}
