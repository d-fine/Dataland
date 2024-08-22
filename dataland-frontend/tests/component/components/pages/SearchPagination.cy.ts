import SearchCompaniesForFrameworkData from '@/components/pages/SearchCompaniesForFrameworkData.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { prepareSimpleDataSearchStoredCompanyArray } from '@ct/testUtils/PrepareDataSearchStoredCompanyArray';
import { type BasicCompanyInformation } from '@clients/backend';

/**
 * Loads mocked data as the intercept response and mounts the component
 * @param mockedResponse inserts a custom dataset as a mocked response
 */
function mockDataAndMountComponent(mockedResponse?: BasicCompanyInformation[]): void {
  const mockDataSearchStoredCompanyArray = prepareSimpleDataSearchStoredCompanyArray(200);
  cy.intercept('GET', '**/api/companies/numberOfCompanies?**', {
    statusCode: 200,
    body: 200,
  });
  cy.intercept('GET', '**/api/companies?**', mockedResponse ?? mockDataSearchStoredCompanyArray);
  cy.intercept('**/api/companies/meta-information', {
    countryCodes: ['CV'],
    sectors: ['partnerships'],
  });
  const keycloakMock = minimalKeycloakMock({
    roles: ['ROLE_USER', 'ROLE_UPLOADER', 'ROLE_REVIEWER'],
  });
  cy.mountWithPlugins<typeof SearchCompaniesForFrameworkData>(SearchCompaniesForFrameworkData, {
    keycloak: keycloakMock,
  }).then((mounted) => {
    void mounted.wrapper.setData({
      resultArray: mockDataSearchStoredCompanyArray,
    });
    cy.wait(500);
  });
}

/**
 * enter text into search bar
 * @param input search string
 */
function enterSearchString(input: string): void {
  cy.get('input[id=search_bar_top]').should('exist').type(input).type('{enter}').should('have.value', input).clear();
}

/**
 * checks if paginator exists
 */
function validateExistenceOfPaginator(): void {
  cy.get('table.p-datatable-table').should('exist');
  cy.get('.p-paginator-current').should('contain.text', 'Showing 1 to 100 of').contains('entries');
  cy.scrollTo('top');
  cy.contains('span', '1-100 of');
}

/**
 * checks if paginator does not exist
 */
function validateAbsenceOfPaginator(): void {
  cy.get('div.p-paginator').should('not.exist');
  cy.contains('span', 'No results');
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

  it('Search for all companies, go to page 2 of the search results, then run a another query and verify that paginator and the page text are reset', () => {
    mockDataAndMountComponent();
    cy.get('button[class="p-paginator-page p-paginator-element p-link"]').eq(0).should('contain.text', '2').click();
    enterSearchString('abs');
    validateExistenceOfPaginator();
  });
});
