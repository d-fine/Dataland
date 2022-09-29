export function fillCompanyUploadFields(companyName: string): void {
  cy.get("input[name=companyName]").type(companyName, { force: true });
  cy.get("input[name=headquarters]").type("Capitol City", { force: true });
  cy.get("input[name=sector]").type("Handmade", { force: true });
  cy.get("input[name=industry]").type("Industry", { force: true });
  cy.get("input[name=countryCode]").type("DE", { force: true });
  cy.get("select[name=identifierType]").select("ISIN");
  cy.get("input[name=identifierValue]").type(`IsinValueId:${crypto.randomUUID()}`, { force: true });
}

export function createCompanyAndGetId(companyName: string): Cypress.Chainable<string> {
  cy.visitAndCheckAppMount("/companies/upload");
  fillCompanyUploadFields(companyName);
  cy.intercept("**/api/companies").as("postCompany");
  cy.get('button[name="postCompanyData"]').click();
  return cy
    .wait("@postCompany")
    .get("body")
    .should("contain", "success")
    .get("span[title=companyId]")
    .then<string>(($companyID) => {
      return $companyID.text();
    });
}
