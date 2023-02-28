import {
  EuTaxonomyDataForFinancials,
  EligibilityKpis,
  DataPointBigDecimal,
  EuTaxonomyDataForFinancialsControllerApi,
  Configuration,
  DataMetaInformation,
  DataTypeEnum,
} from "@clients/backend";
import { FixtureData } from "../fixtures/FixtureUtils";
import Chainable = Cypress.Chainable;

/**
 * Submits the eutaxonomy-financials upload form and checks that the upload completes successfully
 *
 * @returns the resulting cypress chainable
 */
export function submitEuTaxonomyFinancialsUploadForm(): Cypress.Chainable {
  cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyFinancials}`).as("postCompanyAssociatedData");
  cy.get('button[name="postEUData"]').click();
  return cy.wait("@postCompanyAssociatedData").get("body").should("contain", "success");
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
