/**
 * Checks if an image is present
 * @param name the "alt" identifier of the image
 * @param fileName the file the image is expected to display
 * @param parentElement the element whose content to search for the image
 */
export function checkImage(name: string, fileName: string, parentElement: Cypress.Chainable = cy.get("body")): void {
  parentElement
    .find(`img[alt="${name}"]`)
    .should("be.visible")
    .should("have.attr", "src")
    .should("match", new RegExp(`.*/${fileName}$`));
}

/**
 * Checks if a button is present
 * @param name the "name" identifier of the button
 * @param buttonText the string expected to be contained
 * @param parentElement the element whose content to search for the button
 * @returns the chainable on the button element
 */
export function checkButton(
  name: string,
  buttonText: string,
  parentElement: Cypress.Chainable = cy.get("body"),
): Cypress.Chainable {
  return parentElement.find(`button[name="${name}"]`).should("be.visible").should("contain.text", buttonText);
}

/**
 * Checks if a link is present
 * @param anchorText the string expected to be contained
 * @param parentElement the element whose content to search for the link
 * @returns the chainable on the link element
 */
export function checkAnchorByContent(
  anchorText: string,
  parentElement: Cypress.Chainable = cy.get("body"),
): Cypress.Chainable {
  return parentElement.find(`a:contains('${anchorText}')`).should("be.visible");
}

/**
 * Checks if a link is present
 * @param href the "href" identifier of the button
 * @param anchorText the string expected to be contained
 * @param parentElement the element whose content to search for the link
 * @returns the chainable on the link element
 */
export function checkAnchorByTarget(
  href: string,
  anchorText: string,
  parentElement: Cypress.Chainable = cy.get("body"),
): Cypress.Chainable {
  return parentElement.find(`a[href="${href}"]`).should("be.visible").should("contain.text", anchorText);
}
