/**
 * Verifies the header row of the company table
 */
export function verifySearchResultTable(): void {
  cy.get("table.p-datatable-table").contains("th", "COMPANY");
  cy.get("table.p-datatable-table").contains("th", "PERM ID");
  cy.get("table.p-datatable-table").contains("th", "SECTOR");
  cy.get("table.p-datatable-table").contains("th", "LOCATION");
}
