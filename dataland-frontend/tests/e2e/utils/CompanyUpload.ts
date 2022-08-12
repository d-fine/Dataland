export function fillCompanyUploadFields(companyName: string): void {
  cy.get("input[name=companyName]").type(companyName, { force: true });
  cy.get("input[name=headquarters]").type("Capitol City", { force: true });
  cy.get("input[name=sector]").type("Handmade", { force: true });
  cy.get("input[name=marketCap]").type("123", { force: true });
  cy.get("input[name=countryCode]").type("DE", { force: true });
  cy.get("input[name=reportingDateOfMarketCap]").type("2021-09-02", { force: true });
  cy.get("select[name=identifierType]").select("ISIN");
  cy.get("input[name=identifierValue]").type("IsinValueId", { force: true });
}
