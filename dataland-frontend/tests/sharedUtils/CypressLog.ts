/**
 * Logs a message in a way that is visible in CI output.
 * In e2e tests, cy.task('log', ...) is used so the message appears in the Node process stdout.
 * In component tests, cy.task is not available, so cy.log() is used as a fallback.
 * @param message the message to log
 */
export function cyLog(message: string): void {
  if (Cypress.testingType === 'e2e') {
    cy.task('log', message);
  } else {
    cy.log(message);
  }
}

