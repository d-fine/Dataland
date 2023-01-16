import {
  EuTaxonomyDataForFinancials,
  EligibilityKpis,
  DataPointBigDecimal,
  EuTaxonomyDataForFinancialsControllerApi,
  Configuration,
  DataMetaInformation,
} from "@clients/backend";
import { FixtureData } from "../fixtures/FixtureUtils";
import Chainable = Cypress.Chainable;

export function submitEuTaxonomyFinancialsUploadForm(): Cypress.Chainable {
  cy.intercept("**/api/data/eutaxonomy-financials").as("postCompanyAssociatedData");
  cy.get('button[name="postEUData"]').click();
  return cy.wait("@postCompanyAssociatedData").get("body").should("contain", "success");
}

export function uploadDummyEuTaxonomyDataForFinancialsViaForm(companyId: string): Cypress.Chainable {
  //TODO why grey in IDE?  don't we use it?  if not, why not anymore?
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-financials/upload`);
  fillEuTaxonomyFinancialsDummyUploadFields();
  return submitEuTaxonomyFinancialsUploadForm();
}

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

function fillEligibilityKpis(divName: string, data: EligibilityKpis | undefined): void {
  fillField(divName, "taxonomyEligibleActivity", data?.taxonomyEligibleActivity);
  fillField(divName, "taxonomyNonEligibleActivity", data?.taxonomyNonEligibleActivity);
  fillField(divName, "derivatives", data?.derivatives);
  fillField(divName, "banksAndIssuers", data?.banksAndIssuers);
  fillField(divName, "investmentNonNfrd", data?.investmentNonNfrd);
}

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

function fillEuTaxonomyFinancialsDummyUploadFields(): void {
  cy.get("select[name=financialServicesTypes]").select("Credit Institution");
  cy.get("select[name=assurance]").select("Limited Assurance");
  cy.get('input[name="reportingObligation"][value=Yes]').check();
}

export function getFirstEuTaxonomyFinancialsDatasetFromFixtures(): Chainable<EuTaxonomyDataForFinancials> {
  return cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (jsonContent) {
    const companiesWithEuTaxonomyDataForFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
    return companiesWithEuTaxonomyDataForFinancials[0].t;
  });
}

export async function uploadOneEuTaxonomyFinancialsDatasetViaApi(
  token: string,
  companyId: string,
  data: EuTaxonomyDataForFinancials
): Promise<DataMetaInformation> {
  const response = await new EuTaxonomyDataForFinancialsControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedEuTaxonomyDataForFinancials({
    companyId,
    data,
  });
  return response.data;
}
