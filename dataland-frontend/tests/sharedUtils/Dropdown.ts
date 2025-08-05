/**
 * Selects an option from a SingleSelectFormElement by value
 * @param dropdownDiv a div with only a single SingleSelectFormElement inside of it, on which an option will be selected
 * @param valueToSelect string, number or RegExp of the option to select
 * @param exactMatchNotNeeded boolean, if true, the first option to contain the given value will be selected
 */
export function selectItemFromDropdownByValue(
  dropdownDiv: Cypress.Chainable<JQuery<HTMLElement>>,
  valueToSelect: string | number | RegExp,
  exactMatchNotNeeded?: boolean
): void {
  dropdownDiv.find('.p-select-dropdown').click();
  if (exactMatchNotNeeded) {
    cy.get('.p-select-option').contains(valueToSelect).should('contain.text', valueToSelect).click();
  } else {
    cy.get('.p-select-option')
      .contains(new RegExp(`^${valueToSelect}$`))
      .should('have.text', valueToSelect);
    cy.get('.p-select-option')
      .contains(new RegExp(`^${valueToSelect}$`))
      .click();
  }
}

/**
 * Selects an option from a SingleSelectFormElement by index
 * @param dropdownDiv a div with only a single SingleSelectFormElement inside of it, on which an option will be selected
 * @param indexToSelect number, index of the option to be selected
 */
export function selectItemFromDropdownByIndex(
  dropdownDiv: Cypress.Chainable<JQuery<HTMLElement>>,
  indexToSelect: number
): void {
  dropdownDiv.find('.p-select-dropdown').click();
  cy.get('.p-select-list').find('li').eq(indexToSelect).click();
}
