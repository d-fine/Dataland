describe('As a user I expect my api key will be generated correctly', () => {
  /**
   * Verifies that creating an api key works as expected, and also assures that the copy-to-clipboard button works if
   * the Chrome browser is used to execute this cypress test. For other browsers it skips that part of the test.
   */
  function verifyCreatingApiKeyAndCopyingIt(): void {
    cy.get('div.middle-center-div button').contains('CREATE NEW API KEY').click();
    cy.get('div#expiryTime').click();
    cy.get('ul[role="listbox"]').find('[aria-label="No expiry"]').click({ force: true });
    cy.get('button#generateApiKey').click();
    cy.wait('@generateApiKey', { timeout: Cypress.env('short_timeout_in_ms') as number });
    cy.get('[data-test="apiKeyInfo"]').should('exist');
    cy.get('textarea#newKeyHolder').should('exist');
    cy.get('#existingApiKeyCard').find('span').contains('The API Key has no defined expiry date').should('exist');

    if (Cypress.browser.displayName === 'Chrome') {
      cy.wrap(
        Cypress.automation('remote:debugger:protocol', {
          command: 'Browser.grantPermissions',
          params: {
            permissions: ['clipboardReadWrite', 'clipboardSanitizedWrite'],
            origin: window.location.origin,
          },
        })
      );
    }

    cy.get('[data-test="apiKeyInfo"]').find('em').should('exist');
    cy.get('[data-test="text-info"]').find('em').click();

    cy.get('[data-test="text-info"]').find('textarea').should('have.focus');
    cy.get('[data-test="apiKeyInfo"]').find('textarea').should('have.attr', 'readonly');
  }

  /**
   * Verifies that the api key page looks and behaves as expected if you visit it while you already have an api key.
   */
  function verifyAlreadyExistingApiKeyState(): void {
    cy.reload(true);
    cy.wait('@getApiKeyMetaInfoForUser', { timeout: Cypress.env('short_timeout_in_ms') as number });
    cy.url().should('contain', '/api-key');
    cy.get('[data-test="regenerateApiKeyMessage"]').should('exist');
    cy.get('textarea#newKeyHolder').should('not.exist');
    cy.get('[data-test="text-info"]').should(
      'contain',
      "If you don't have access to your API Key you can generate a new one"
    );
    cy.get('[id="apiKeyUsageInfoMessage"]').should('contain', 'You can use the API-Keys as bearer-tokens');
    cy.get('[data-test="action-button"]').should('contain', 'REGENERATE API KEY').click();
    cy.get('div#regenerateApiKeyModal').should('be.visible').find('[data-test="regenerateApiKeyCancelButton"]').click();
    cy.get('div#regenerateApiKeyModal').should('not.exist');
    cy.get('[data-test="action-button"]').should('contain', 'REGENERATE API KEY').click();
    cy.get('div#regenerateApiKeyModal')
      .should('be.visible')
      .find('[data-test="regenerateApiKeyConfirmButton"]')
      .click();
    cy.get('h1').should('contain.text', 'Create new API Key');
  }

  it('Check Api Key functionalities', () => {
    cy.ensureLoggedIn();
    cy.intercept('GET', '**/api-keys/getApiKeyMetaInfoForUser*').as('getApiKeyMetaInfoForUser');
    cy.intercept('GET', '**/api-keys/generateApiKey*').as('generateApiKey');
    cy.visitAndCheckAppMount('/api-key');
    cy.wait('@getApiKeyMetaInfoForUser', { timeout: Cypress.env('medium_timeout_in_ms') as number });

    verifyCreatingApiKeyAndCopyingIt();

    verifyAlreadyExistingApiKeyState();
  });
});
