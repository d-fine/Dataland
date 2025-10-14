import SearchCompaniesForFrameworkData from '@/components/pages/SearchCompaniesForFrameworkData.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { verifySearchResultTableExists } from '@sharedUtils/ElementChecks';
import { type BasicCompanyInformation } from '@clients/backend';

let mockDataSearchResponse: Array<BasicCompanyInformation>;

before(function () {
  cy.fixture('DataSearchStoredCompanyMocks').then(function (jsonContent) {
    mockDataSearchResponse = jsonContent as Array<BasicCompanyInformation>;
  });
});

describe('Component tests for the Dataland companies search page', function (): void {
  beforeEach(() => {
    cy.intercept('**/api/companies?**', mockDataSearchResponse);
    cy.intercept('**/api/companies/meta-information', {});
  });

  it('Check static layout of the search page', function () {
    cy.mountWithPlugins(SearchCompaniesForFrameworkData, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      const placeholder = 'Search company by name or identifier (e.g. PermID, LEI, ...)';
      const inputValue = 'A company name';
      cy.get('input[id=search-bar-input]')
        .should('not.be.disabled')
        .type(inputValue)
        .should('have.value', inputValue)
        .invoke('attr', 'placeholder')
        .should('contain', placeholder);
    });
  });

  it('Check correct behaviour of search bar when scrolling', { scrollBehavior: false }, function () {
    cy.mountWithPlugins(SearchCompaniesForFrameworkData, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      verifySearchResultTableExists();
      cy.get('div[class="button-container"]').should('be.visible');

      cy.scrollTo(0, 500, { duration: 200 });
      cy.get('input[id=search-bar-input]').should('exist');
      cy.get('div[class="button-container"]').should('not.exist');

      cy.scrollTo(0, 0, { duration: 200 });
      cy.get('input[id=search-bar-input]').should('exist');
      cy.get('div[class="button-container"]').should('be.visible').click();
    });
  });

  it(
    'Scroll the page to type into the search bar in different states and check if the input is always saved',
    { scrollBehavior: false },
    () => {
      cy.mountWithPlugins(SearchCompaniesForFrameworkData, {
        keycloak: minimalKeycloakMock({}),
      }).then(() => {
        const inputValue1 = 'ABCDEFG';
        const inputValue2 = 'XYZ';
        verifySearchResultTableExists();
        cy.get('input[id=search-bar-input]').type(inputValue1);
        cy.scrollTo(0, 500, { duration: 200 });
        cy.get('input[id=search-bar-input]').should('have.value', inputValue1).type(inputValue2);
        cy.scrollTo(0, 0, { duration: 200 });
        cy.get('input[id=search-bar-input]').should('have.value', inputValue1 + inputValue2);
      });
    }
  );
});
