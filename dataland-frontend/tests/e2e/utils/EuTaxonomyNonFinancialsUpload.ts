export function fillEuTaxonomyNonFinancialsDummyUploadFields(): void {
  cy.get("select[name=attestation]").select("Limited Assurance");
  cy.get('input[id="reportingObligation-option-yes"][value=Yes]').check({ force: true });
  for (const argument of ["capex", "opex", "revenue"]) {
    cy.get(`div[title=${argument}] input[name=eligiblePercentage]`).type("0.657");
    cy.get(`div[title=${argument}] input[name=totalAmount]`).type("120000000");
  }
}

export function uploadDummyEuTaxonomyDataForNonFinancials(companyId: string): Cypress.Chainable<string> {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-non-financials/upload`);
  fillEuTaxonomyNonFinancialsDummyUploadFields();
  cy.intercept("**/api/data/eutaxonomy-non-financials").as("postCompanyAssociatedData");
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
