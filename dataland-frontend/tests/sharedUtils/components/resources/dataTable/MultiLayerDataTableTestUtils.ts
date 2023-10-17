/**
 * Retrieves the section header row which contains the passed data-section-label
 * @param label the data-section-label of a table row
 * @param isExpectedToBeVisible describes whether this section header is expected to be visible
 * @returns the table row element
 */
export function getSectionHead(label: string, isExpectedToBeVisible = true): Cypress.Chainable {
  return cy.get(`tr[data-section-label='${label}']${isExpectedToBeVisible ? ":visible" : ""}`);
}

/**
 * Retrieves the cell container with the given label and dataset index
 * @param label the label of the cell to retrieve
 * @param datasetIdx the index of dataset to retrieve
 * @param isExpectedToBeVisible describes whether the cell container is expected to be visible
 * @returns the cell
 */
export function getCellContainer(label: string, datasetIdx = 0, isExpectedToBeVisible = true): Cypress.Chainable {
  return cy.get(
    `td[data-cell-label='${label}'][data-dataset-index='${datasetIdx}']${isExpectedToBeVisible ? ":visible" : ""}`,
  );
}

/**
 * Retrieves the row header of the row with the given label
 * @param label the label of the cell to retrieve
 * @returns the row header
 */
export function getRowHeader(label: string): Cypress.Chainable {
  return cy.get(`td[data-cell-label='${label}'][data-row-header="true"]`); // TODO for what?  Re-use in other functions?
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
  isIconExpectedToBeVisible: boolean,
): Cypress.Chainable {
  const sectionHead = getSectionHead(label);
  if (isIconExpectedToBeVisible) {
    sectionHead.find("i[data-test=hidden-icon]").should("be.visible");
  } else {
    sectionHead.find("i[data-test=hidden-icon]").should("not.exist");
  }
  return getSectionHead(label);
}

/**
 * Retrieves the cell with the given label and dataset index and checks for the visibility of a hidden-icon
 * attached to it.
 * @param label the label of the cell to retrieve
 * @param isIconExpectedToBeVisible describes whether the hidden-icon is expected to be visible or not
 * @param datasetIdx the index of dataset to retrieve
 * @returns the cell container itself in order to make it possible to chain other commands
 */
export function getCellContainerAndCheckIconForHiddenDisplay(
  label: string,
  isIconExpectedToBeVisible: boolean,
  datasetIdx = 0,
): Cypress.Chainable {
  const cellContainer = getCellContainer(label, datasetIdx);
  if (isIconExpectedToBeVisible) {
    cellContainer.find("i[data-test=hidden-icon]").should("be.visible");
  } else {
    cellContainer.find("i[data-test=hidden-icon]").should("not.exist");
  }
  return getCellContainer(label, datasetIdx);
}
