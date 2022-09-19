import {
  EuTaxonomyDataForFinancials,
  EligibilityKpis,
  DataPoint,
} from "../../../build/clients/backend/org/dataland/datalandfrontend/openApiClient/model";

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

export function generateEuTaxonomyUpload(data: EuTaxonomyDataForFinancials) {
  cy.get("select[name=financialServicesTypes]").select(data.financialServicesTypes!!);
  cy.get("select[name=assurance]").select(data.assurance!!.assurance);
  cy.get(`input[name="reportingObligation"][value=${data.reportingObligation!!.toString()}]`).check();
  if (data.eligibilityKpis !== undefined) {
    if (data.eligibilityKpis.CreditInstitution !== undefined) {
      fillEligibilityKpis("CreditInstitution", data.eligibilityKpis.CreditInstitution);
    }
    if (data.eligibilityKpis.InsuranceOrReinsurance !== undefined) {
      fillEligibilityKpis("InsuranceOrReinsurance", data.eligibilityKpis.InsuranceOrReinsurance);
    }
    if (data.eligibilityKpis.AssetManagement !== undefined) {
      fillEligibilityKpis("AssetManagement", data.eligibilityKpis.AssetManagement);
    }
  }
  if (data.insuranceKpis !== undefined) {
    fillField(
      "",
      "taxonomyEligibleNonLifeInsuranceActivities",
      data.insuranceKpis.taxonomyEligibleNonLifeInsuranceActivities
    );
  }
  if (data.creditInstitutionKpis !== undefined) {
    fillField("", "tradingPortfolioAndInterbankLoans", data.creditInstitutionKpis.tradingPortfolioAndInterbankLoans);
    fillField("", "tradingPortfolio", data.creditInstitutionKpis.tradingPortfolio);
    fillField("", "interbankLoans", data.creditInstitutionKpis.interbankLoans);
  }
}

function fillEligibilityKpis(divName: string, data: EligibilityKpis) {
  fillField(divName, "taxonomyEligibleActivity", data.taxonomyEligibleActivity);
  fillField(divName, "derivatives", data.derivatives);
  fillField(divName, "banksAndIssuers", data.banksAndIssuers);
  fillField(divName, "investmentNonNfrd", data.investmentNonNfrd);
}

function fillField(divName: string, inputName: string, value?: DataPoint) {
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
