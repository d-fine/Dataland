/**
 * Checks if an image is present
 * @param name the "alt" identifier of the image
 * @param file the file the image is expected to display
 */
export function checkImage(name: string, file: string): void {
  cy.get(`img[alt="${name}"]`).should("be.visible").should("have.attr", "src").should("include", file);
}

/**
 * Checks if a button is present
 * @param name the "name" identifier of the button
 * @param message the string expected to be contained
 */
export function checkButton(name: string, message: string): void {
  cy.get(`button[name=${name}]`).should("be.visible").should("contain.text", message);
}
