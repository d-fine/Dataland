export function fillEuTaxonomyFinancialsUploadFields(): void {
  cy.get("select[name=financialServicesType]").select("Credit Institution");
  cy.get("select[name=attestation]").select("Limited Assurance");
  cy.get('input[id="reportingObligation-option-yes"][value=Yes]').check({ force: true });
  cy.get("input[name=taxonomyEligibleActivity]").type("0.5", { force: true });
  cy.get("input[name=derivatives]").type("0.1", { force: true });
  cy.get("input[name=banksAndIssuers]").type("0.3", { force: true });
  cy.get("input[name=investmentNonNfrd]").type("0.25", { force: true });
  cy.get("input[name=tradingPortfolio]").type("0.6", { force: true });
  cy.get("input[name=interbankLoans]").type("0.4", { force: true });
}

export function fillEuTaxonomyNonFinancialsUploadFields(): void {
  cy.get("select[name=attestation]").select("Limited Assurance");
  cy.get('input[id="reportingObligation-option-yes"][value=Yes]').check({ force: true });
  for (const argument of ["capex", "opex", "revenue"]) {
    cy.get(`div[title=${argument}] input[name=eligiblePercentage]`).type("0.657");
    cy.get(`div[title=${argument}] input[name=totalAmount]`).type("120000000");
  }
}

export function uploadEuTaxonomyDataForNonFinancials(companyId: string): Cypress.Chainable<string> {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-non-financials/upload`);
  fillEuTaxonomyNonFinancialsUploadFields();
  cy.intercept("**/api/data/eutaxonomy/nonfinancials").as("postCompanyAssociatedData");
  cy.get('button[name="postEUData"]').click();
  return cy
    .wait("@postCompanyAssociatedData")
    .get("body")
    .should("contain", "success")
    .get("span[title=dataId]")
    .then<string>(($dataId) => {
      const id = $dataId.text();
      return id;
    });
}

export function uploadEuTaxonomyDataForFinancials(companyId: string): Cypress.Chainable<string> {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-financials/upload`);
  fillEuTaxonomyFinancialsUploadFields();
  cy.intercept("**/api/data/eutaxonomy/financials").as("postCompanyAssociatedData");
  cy.get('button[name="postEUData"]').click();
  return cy
    .wait("@postCompanyAssociatedData")
    .get("body")
    .should("contain", "success")
    .get("span[title=dataId]")
    .then<string>(($dataId) => {
      const id = $dataId.text();
      return id;
    });
}
