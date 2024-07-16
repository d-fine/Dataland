// @ts-nocheck
import ApiKeysPage from '@/components/pages/ApiKeysPage.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

describe('Component test for ApiKeyCard.vue', () => {
  it('If api key already exists, it should display proper user role', () => {
    cy.intercept('GET', '**/api-keys/getApiKeyMetaInfoForUser', { fixture: 'ApiKeyInfoMockWithKey.json' });
    cy.mountWithPlugins(ApiKeysPage, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          newKey: 'abcdefghijklmnoprstwxyz123456789',
          userAlreadyHasApiKey: true,
          pageState: 'view',
          waitingForData: false,
        };
      },
    });
    cy.get('[data-test="apiKeyInfo"]').find('textarea').should('have.attr', 'readonly');
    cy.get('[data-test="apiKeyInfo"]').find('textarea').invoke('val').should('eq', 'abcdefghijklmnoprstwxyz123456789');
  });

  it(
    'If api key does not exist yet, you can begin to create one, set an expiration date in the dropdown, ' +
      'or close the creation again',
    () => {
      cy.intercept('GET', '**/api-keys/getApiKeyMetaInfoForUser', { fixture: 'ApiKeyInfoMockWithNOKey.json' });
      cy.stub(TheHeader);
      cy.mountWithPlugins(ApiKeysPage, {
        keycloak: minimalKeycloakMock({}),
      });
      cy.get("[data-test='noApiKeyWelcomeComponent']").should('exist').should('contain.text', 'You have no API Key!');
      cy.get("[data-test='noApiKeyWelcomeComponent']")
        .find('button')
        .should('contain.text', 'CREATE NEW API KEY')
        .click();
      cy.get('[data-test="CreateApiKeyCard"]').should('exist');
      cy.get('h1').should('contain.text', 'Create new API Key');
      cy.get('[data-test="cancelGenerateApiKey"]').click();
      cy.get('h1').should('contain.text', 'API Key');
      cy.get('[data-test="CreateApiKeyCard"]').should('not.exist');

      cy.get('div.middle-center-div button').contains('CREATE NEW API KEY').click();
      cy.get('button#generateApiKey').click();
      cy.get('label[for="expiryTime"]').should('contain.text', `Please select expiration date`);
      cy.get('div#expiryTime').click();
      cy.get('ul[role="listbox"]').find('[aria-label="Custom..."]').click();
      cy.get('label[for="expiryTime"]').should('not.contain.text', `Please select expiration date`);
      cy.get('button#generateApiKey').click();
      cy.get('label[for="expiryTime"]').should('contain.text', `Please select expiration date`);
      cy.get('div#expiryTime').click();
      cy.get('ul[role="listbox"]').find('[aria-label="7 days"]').click();
      cy.get('#expiryTimeWrapper').should('contain.text', `The API Key will expire on`);
      cy.get('div#expiryTime').click();
      cy.get('ul[role="listbox"]').find('[aria-label="Custom..."]').click({ force: true });
      cy.get('#expiryTimeWrapper').should('not.exist');
      cy.get('[data-test="expiryDatePicker"]').should('be.visible');
      cy.get('button.p-datepicker-trigger').click();
      cy.get('div.p-datepicker').find('button[aria-label="Next Month"]').click();
      cy.get('div.p-datepicker').find('span:contains("13")').click();
      cy.get('[data-test="expiryDatePicker"]')
        .find('input')
        .should(($input) => {
          const val = $input.val();
          expect(val).to.include('13');
        });
      cy.get('[data-test="cancelGenerateApiKey"]').click();
    }
  );
});
