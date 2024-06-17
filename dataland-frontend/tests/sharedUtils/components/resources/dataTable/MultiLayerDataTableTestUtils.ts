/**
 * Retrieves the section header row which contains the passed data-section-label
 * @param label the data-section-label of a table row
 * @param isExpectedToBeVisible describes whether this section header is expected to be visible
 * @returns the table row element
 */
export function getSectionHead(label: string, isExpectedToBeVisible = true): Cypress.Chainable {
  return cy.get(`tr[data-section-label='${label}']${isExpectedToBeVisible ? ':visible' : ''}`);
}

/**
 * Retrieves the cell value container with the given label and dataset index
 * @param label the label of the cell value container to retrieve
 * @param datasetIdx the index of dataset to retrieve
 * @param isExpectedToBeVisible describes whether the cell value container is expected to be visible
 * @returns the cell value container
 */
export function getCellValueContainer(label: string, datasetIdx = 0, isExpectedToBeVisible = true): Cypress.Chainable {
  return cy.get(
    `td[data-cell-label='${label}'][data-dataset-index='${datasetIdx}']${isExpectedToBeVisible ? ':visible' : ''}`
  );
}

/**
 * Retrieves the row header container for a cell with the given label
 * @param label the label of the cell for which the row header shall be retrieved
 * @returns the cell row header container
 */
export function getCellRowHeaderContainer(label: string): Cypress.Chainable {
  return cy.get(`td[data-cell-label='${label}'][data-row-header="true"]`);
}

/**
 * Retrieves the section header row which contains the passed data-section-label and checks for the visibility of a
 * hidden-icon attached to it.
 * @param label the data-section-label of a table row
 * @param isIconExpectedToBeVisible describes whether the hidden-icon is expected to be visible or not
 * @returns the section head itself in order to make it possible to chain other commands
 */
export function getSectionHeadAndCheckIconForHiddenDisplay(
  label: string,
  isIconExpectedToBeVisible: boolean
): Cypress.Chainable {
  const sectionHead = getSectionHead(label);
  if (isIconExpectedToBeVisible) {
    sectionHead.find('i[data-test=hidden-icon]').should('be.visible');
  } else {
    sectionHead.find('i[data-test=hidden-icon]').should('not.exist');
  }
  return getSectionHead(label);
}

/**
 * Retrieves the cell value container with the given label and dataset index and checks for the visibility of a
 * hidden-icon attached to it.
 * @param label the label of the cell to retrieve
 * @param isIconExpectedToBeVisible describes whether the hidden-icon is expected to be visible or not
 * @param datasetIdx the index of dataset to retrieve
 * @returns the cell value container itself in order to make it possible to chain other commands
 */
export function getCellValueContainerAndCheckIconForHiddenDisplay(
  label: string,
  isIconExpectedToBeVisible: boolean,
  datasetIdx = 0
): Cypress.Chainable {
  const cellValueContainer = getCellValueContainer(label, datasetIdx);
  if (isIconExpectedToBeVisible) {
    cellValueContainer.find('i[data-test=hidden-icon]').should('be.visible');
  } else {
    cellValueContainer.find('i[data-test=hidden-icon]').should('not.exist');
  }
  return getCellValueContainer(label, datasetIdx);
}
