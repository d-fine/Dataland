import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import GetHelpDialog from '@/components/resources/portfolio/GetHelpDialog.vue';

describe('Check the Get Help form', () => {
  beforeEach(() => {
    // @ts-ignore
    cy.mountWithPlugins(GetHelpDialog, {
      keycloak: minimalKeycloakMock({}),
    });
  });

  it('Should display the form with initial empty values', () => {
    cy.get('#get-help-topic').should('have.value', '');
    cy.get('#get-help-message').should('have.value', '');
    cy.get('.send-button').should('be.disabled');
  });

  it('Should validate the form: enable button when topic and message are provided', () => {
    // Select a topic
    cy.get('#get-help-topic').click();
    cy.get('.p-dropdown-item').contains('Find company identifiers').click();
    cy.get('#get-help-topic').should('contain.text', 'Find company identifiers');

    // Enter a message
    cy.get('#get-help-message').type('I need help with finding identifiers.');
    cy.get('#get-help-message').should('have.value', 'I need help with finding identifiers.');

    // Validate that the send button is enabled
    cy.get('.send-button').should('not.be.disabled');
  });

  it('Should show an error message if required fields are missing', () => {
    cy.get('.send-button').should('be.disabled');
    cy.get('.p-message-error').should('contain.text', 'Please choose a topic and enter a message to us.');
  });

  it('Should send the email successfully and show a success message', () => {
    // Mock API response for successful email request
    cy.intercept('POST', '**/portfolios/support', {
      statusCode: 200,
      body: { success: true },
    }).as('sendSupportRequest');

    // Fill out the form
    cy.get('#get-help-topic').click();
    cy.get('.p-dropdown-item').contains('Find company identifiers').click();
    cy.get('#get-help-message').type('I need help with finding identifiers.');

    // Send the email
    cy.get('.send-button').click();
    cy.wait('@sendSupportRequest');

    // Confirm success message is displayed
    cy.get('.p-message-success').should('contain.text', 'Thank you for contacting us. We have received your request.');
  });

  it('Should handle API errors gracefully', () => {
    // Mock API response for failed email request
    cy.intercept('POST', '**/portfolios/support', {
      statusCode: 500,
      body: { message: 'Internal server error' },
    }).as('sendSupportRequest');

    // Fill out the form
    cy.get('#get-help-topic').click();
    cy.get('.p-dropdown-item').contains('Other topic').click();
    cy.get('#get-help-message').type('This is a test message.');

    // Attempt to send the email
    cy.get('.send-button').click();
    cy.wait('@sendSupportRequest');

    // Confirm error message is displayed
    cy.get('.p-message-error').should('contain.text', 'Request failed with status code 500');
  });
});
