import { describeIf } from '@e2e/support/TestUtility';

describe('As a user, I expect the cookie banner to render and to be functional', () => {
  describeIf(
    'Do not execute these tests in the CI pipeline, because the github actions server is not registered on the cookiebot.com website',
    {
      executionEnvironments: ['developmentLocal'],
    },
    () => {
      it('Check that the cookie banner renders on first visit', () => {
        cy.visit('/');
        cy.get('#CybotCookiebotDialog').should('be.visible');
        cy.get("button[id='CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll']").should('exist');
        cy.get("button[id='CybotCookiebotDialogBodyLevelButtonLevelOptinAllowallSelection']").should('exist');
        cy.get("button[id='CybotCookiebotDialogBodyButtonDecline']").should('exist');

        cy.get('#CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll').click();
        cy.get('#CybotCookiebotDialog').should('not.be.visible');
      });

      it('Check that the Cookie Settings button reopens the Cookiebot dialog after cookies have been accepted', () => {
        cy.visit('/');
        cy.get('#CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll').click();
        cy.get('#CybotCookiebotDialog').should('not.be.visible');

        cy.get("button:contains('Cookie Settings')").should('exist').click();
        cy.get('#CybotCookiebotDialog').should('be.visible');
      });
    }
  );
});
