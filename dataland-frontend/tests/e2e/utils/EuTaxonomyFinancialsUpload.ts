import { EuTaxonomyDataForFinancials, EligibilityKpis, DataPointBigDecimal } from "@clients/backend";

export function submitEuTaxonomyFinancialsUploadForm(): Cypress.Chainable {
  cy.intercept("**/api/data/eutaxonomy-financials").as("postCompanyAssociatedData");
  cy.get('button[name="postEUData"]').click();
  return cy.wait("@postCompanyAssociatedData").get("body").should("contain", "success");
}

export function uploadDummyEuTaxonomyDataForFinancials(companyId: string): Cypress.Chainable {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-financials/upload`);
  fillEuTaxonomyFinancialsDummyUploadFields();
  return submitEuTaxonomyFinancialsUploadForm();
}

export function generateEuTaxonomyUpload(data: EuTaxonomyDataForFinancials): void {
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
  fillField(
    "",
    "taxonomyEligibleNonLifeInsuranceActivities",
    data.insuranceKpis?.taxonomyEligibleNonLifeInsuranceActivities
  );
  fillField("", "tradingPortfolioAndInterbankLoans", data.creditInstitutionKpis?.tradingPortfolioAndInterbankLoans);
  fillField("", "tradingPortfolio", data.creditInstitutionKpis?.tradingPortfolio);
  fillField("", "interbankLoans", data.creditInstitutionKpis?.interbankLoans);
}

function fillEligibilityKpis(divName: string, data: EligibilityKpis | undefined): void {
  fillField(divName, "taxonomyEligibleActivity", data?.taxonomyEligibleActivity);
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
