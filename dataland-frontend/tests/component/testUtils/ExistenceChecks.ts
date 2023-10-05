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
 * @param message the string expected to be contained
 * @param parentElement the element whose content to search for the button
 */
export function checkButton(name: string, message: string, parentElement: Cypress.Chainable = cy.get("body")): void {
  parentElement.find(`button[name="${name}"]`).should("be.visible").should("contain.text", message);
}

/**
 * Checks if a link is present
 * @param name the "name" identifier of the button
 * @param message the string expected to be contained
 * @param parentElement the element whose content to search for the link
 * @returns the chainable on the link element
 */
export function checkLinkByName(
  name: string,
  message: string,
  parentElement: Cypress.Chainable = cy.get("body"),
): Cypress.Chainable {
  return parentElement.find(`a[name="${name}"]`).should("be.visible").should("contain.text", message);
}

/**
 * Checks if a link is present
 * @param href the "href" identifier of the button
 * @param message the string expected to be contained
 * @param parentElement the element whose content to search for the link
 * @returns the chainable on the link element
 */
export function checkLinkByTarget(
  href: string,
  message: string,
  parentElement: Cypress.Chainable = cy.get("body"),
): Cypress.Chainable {
  return parentElement.find(`a[href="${href}"]`).should("be.visible").should("contain.text", message);
}
