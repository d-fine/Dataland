import SearchCompaniesForFrameworkData from '@/components/pages/SearchCompaniesForFrameworkData.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import type Keycloak from 'keycloak-js';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakUtils';
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

  /**
   * Method to check the existence and the redirect-functionality of the Bulk Request Data button
   * @param keycloakMock to be used for the login status
   */
  function verifyExistenceAndFunctionalityOfBulkDataRequestButton(keycloakMock: Keycloak): void {
    cy.mountWithPlugins(SearchCompaniesForFrameworkData, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      cy.wait(500);
      cy.get('button').contains('BULK DATA REQUEST').should('exist').click({ force: true });
      cy.wrap(mounted.component).its('$route.path').should('eq', '/bulkdatarequest');
    });
  }

  it('Check static layout of the search page', function () {
    cy.mountWithPlugins(SearchCompaniesForFrameworkData, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      const placeholder = 'Search company by name or identifier (e.g. PermID, LEI, ...)';
      const inputValue = 'A company name';
      cy.get('input[id=search_bar_top]')
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
      cy.get('button[name=search_bar_collapse]').should('not.be.visible');

      cy.scrollTo(0, 500, { duration: 200 });
      cy.get('input[id=search_bar_top]').should('exist');
      cy.get('button[name=search_bar_collapse]').should('be.visible');

      cy.scrollTo(0, 0, { duration: 200 });
      cy.get('input[id=search_bar_top]').should('exist');
      cy.get('button[name=search_bar_collapse]').should('not.be.visible');

      cy.scrollTo(0, 500, { duration: 200 });
      cy.get('button[name=search_bar_collapse]').should('exist').click();
      cy.get('input[id=search_bar_top]').should('not.exist');
      cy.get('input[id=search_bar_scrolled]').should('exist');
      cy.get('button[name=search_bar_collapse]').should('not.be.visible');

      cy.scrollTo(0, 480, { duration: 200 });
      cy.get('button[name=search_bar_collapse]').should('be.visible');
      cy.get('input[id=search_bar_top]').should('exist');
      cy.get('input[id=search_bar_scrolled]').should('not.exist');
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
        cy.get('input[id=search_bar_top]').type(inputValue1);
        cy.scrollTo(0, 500, { duration: 200 });
        cy.get('button[name=search_bar_collapse]').click();
        cy.get('input[id=search_bar_scrolled]').should('have.value', inputValue1).type(inputValue2);
        cy.scrollTo(0, 0, { duration: 200 });
        cy.get('input[id=search_bar_top]').should('have.value', inputValue1 + inputValue2);
      });
    }
  );

  it("Check that the 'Bulk Request Data' button exists and works as expected for a data reader", () => {
    const keycloakMock = minimalKeycloakMock({});
    verifyExistenceAndFunctionalityOfBulkDataRequestButton(keycloakMock);
  });

  it("Check that the 'Bulk Request Data' button exists and works as expected for uploaders and reviewers", () => {
    const keycloakMock = minimalKeycloakMock({
      roles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_REVIEWER],
    });
    verifyExistenceAndFunctionalityOfBulkDataRequestButton(keycloakMock);
  });
});
