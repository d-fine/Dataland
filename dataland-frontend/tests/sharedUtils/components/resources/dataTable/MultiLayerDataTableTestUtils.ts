/**
 * Retrieves the header of the section with the given label
 * @param label the label of the section to retrieve
 * @returns the section header
 */
export function getSectionHead(label: string): Cypress.Chainable {
  return cy.get(`tr[data-section-label='${label}']`);
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
