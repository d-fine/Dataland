/**
 * Checks if an image is present
 * @param alternativeText the "alt" identifier of the image
 * @param fileName the file the image is expected to display
 */
export function checkImage(alternativeText: string, fileName: string): void {
  cy.get(`img[alt="${alternativeText}"]`)
    .should('be.visible')
    .should('have.attr', 'src')
    .should('match', new RegExp(`.*/${fileName}$`));
}

/**
 * Checks if a button is present
 * @param name the "name" identifier of the button
 * @param buttonText the string expected to be contained
 * @returns the chainable on the button element
 */
export function checkButton(name: string, buttonText: string): Cypress.Chainable {
  return cy.get(`button[name="${name}"]`).should('be.visible').should('contain.text', buttonText);
}

/**
 * Checks if a link is present
 * @param anchorText the string expected to be contained
 * @returns the chainable on the link element
 */
export function checkAnchorByContent(anchorText: string): Cypress.Chainable {
  return cy.get(`a:contains('${anchorText}')`).should('be.visible');
}

/**
 * Checks if a link is present
 * @param href the "href" identifier of the button
 * @param anchorText the string expected to be contained
 * @returns the chainable on the link element
 */
export function checkAnchorByTarget(href: string, anchorText: string): Cypress.Chainable {
  return cy.get(`a[href="${href}"]`).should('be.visible').should('contain.text', anchorText);
}
