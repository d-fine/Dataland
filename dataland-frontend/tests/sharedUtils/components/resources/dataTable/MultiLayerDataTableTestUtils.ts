/**
 * Retrieves the section header row which contains the passed data-section-label
 * @param label the data-section-label of a table row
 * @param isExpectedToBeVisible describes whether this row is expected to be visible
 * @returns the table row element
 */
export function getSectionHead(label: string, isExpectedToBeVisible = true): Cypress.Chainable {
  return cy.get(`tr[data-section-label='${label}']${isExpectedToBeVisible ? ":visible" : ""}`);
}

/**
 * Retrieves the cell with the given label and dataset index
 * @param label the label of the cell to retrieve
 * @param datasetIdx the index of dataset to retrieve
 * @returns the cell
 */
export function getCellContainer(label: string, datasetIdx = 0): Cypress.Chainable {
  return cy.get(`td[data-cell-label='${label}'][data-dataset-index='${datasetIdx}']`);
}

/**
 * Retrieves the row header of the row with the given label
 * @param label the label of the cell to retrieve
 * @returns the row header
 */
export function getRowHeader(label: string): Cypress.Chainable {
  return cy.get(`td[data-cell-label='${label}'][data-row-header="true"]`);
}
