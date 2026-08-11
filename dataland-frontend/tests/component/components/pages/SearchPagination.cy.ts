import SearchCompaniesForFrameworkData from '@/components/pages/SearchCompaniesForFrameworkData.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { prepareSimpleDataSearchStoredCompanyArray } from '@ct/testUtils/PrepareDataSearchStoredCompanyArray';
import { type BasicCompanyInformation } from '@clients/backend';

/**
 * Loads mocked data as the intercept response and mounts the component.
 *
 * @param mockedResponse inserts a custom dataset as a mocked response
 */
function mockDataAndMountComponent(mockedResponse?: BasicCompanyInformation[]): void {
  const mockDataSearchStoredCompanyArray = prepareSimpleDataSearchStoredCompanyArray(200);
  const companyResponse = mockedResponse ?? mockDataSearchStoredCompanyArray;

  cy.intercept('GET', '**/api/companies/numberOfCompanies?**', {
    statusCode: 200,
    body: 200,
  }).as('getNumberOfCompanies');

  cy.intercept('GET', '**/api/companies?**', {
    statusCode: 200,
    body: companyResponse,
  }).as('getCompanies');

  cy.intercept('**/api/companies/meta-information', {
    statusCode: 200,
    body: {
      countryCodes: ['CV'],
      sectors: ['partnerships'],
    },
  }).as('getCompanyMetaInformation');

  const keycloakMock = minimalKeycloakMock({
    roles: ['ROLE_USER', 'ROLE_UPLOADER', 'ROLE_REVIEWER'],
  });

  cy.mountWithPlugins<typeof SearchCompaniesForFrameworkData>(SearchCompaniesForFrameworkData, {
    keycloak: keycloakMock,
  }).then((mounted) => {
    return mounted.wrapper.setData({
      resultArray: mockDataSearchStoredCompanyArray,
    });
  });

  cy.get('input[id="search-bar-input"]').should('be.visible');
}

/**
 * Enters text into the search bar.
 *
 * @param input search string
 */
function enterSearchString(input: string): void {
  cy.get('input[id="search-bar-input"]')
    .should('be.visible')
    .clear()
    .type(input)
    .type('{enter}')
    .should('have.value', input);
}

/**
 * Checks if the paginator exists.
 */
function validateExistenceOfPaginator(): void {
  cy.get('table.p-datatable-table').should('exist');

  cy.get('.p-paginator-current').should('contain.text', 'Showing 1 to 100 of').and('contain.text', 'entries');

  cy.scrollTo('top');

  cy.contains('span', '1-100 of').should('be.visible');
}

/**
 * Checks if the paginator does not exist.
 */
function validateAbsenceOfPaginator(): void {
  cy.get('div.p-paginator').should('not.exist');
  cy.contains('span', 'No results').should('be.visible');
}

describe('As a user, I expect there to be multiple result pages if there are many results to be displayed', () => {
  it('Do a search with 0 matches, then assure that the paginator is gone and the page text says no results', () => {
    mockDataAndMountComponent([]);

    enterSearchString('ABCDEFGHIJKLMNOPQRSTUVWXYZ12345678987654321');
    validateAbsenceOfPaginator();
  });

  it("Search for all companies containing 'abs' and verify that results are paginated, only first 100 are shown", () => {
    mockDataAndMountComponent();

    enterSearchString('abs');
    validateExistenceOfPaginator();
  });

  it('Search for all companies, go to page 2 of the search results, then run another query and verify that the paginator and page text are reset', () => {
    mockDataAndMountComponent();

    cy.get('.p-paginator-pages').find('button.p-paginator-page').eq(0).should('contain.text', '2').click();

    enterSearchString('abs');
    validateExistenceOfPaginator();
  });
});
