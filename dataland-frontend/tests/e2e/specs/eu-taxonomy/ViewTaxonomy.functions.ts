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
