export function verifyTaxonomySearchResultTable(): void {
  cy.get("table.p-datatable-table").contains("th", "COMPANY");
  cy.get("table.p-datatable-table").contains("th", "PERM ID");
  cy.get("table.p-datatable-table").contains("th", "SECTOR");
  cy.get("table.p-datatable-table").contains("th", "MARKET CAP");
  cy.get("table.p-datatable-table").contains("th", "LOCATION");
}

export function checkViewButtonWorks(): void {
  cy.get("table.p-datatable-table")
    .contains("td", "VIEW")
    .contains("a", "VIEW")
    .click()
    .url()
    .should("include", "/companies/")
    .url()
    .should("include", "/eutaxonomies");
}
