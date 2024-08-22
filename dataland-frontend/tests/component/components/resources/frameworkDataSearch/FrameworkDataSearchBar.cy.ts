// @ts-nocheck
import FrameworkDataSearchBar from '@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type BasicCompanyInformation } from '@clients/backend';

let modifiedMockDataSearchResponse: Array<BasicCompanyInformation>;
const highlightedSubString = 'this_is_expected_to_be_highlighted';
before(function () {
  cy.fixture('DataSearchStoredCompanyMocks').then(function (jsonContent) {
    const mockDataSearchResponse = jsonContent as Array<BasicCompanyInformation>;
    const customCompanyName = 'ABCDEFG' + highlightedSubString + 'HIJKLMNOP';
    modifiedMockDataSearchResponse = [...mockDataSearchResponse.slice(0, 4)];
    modifiedMockDataSearchResponse[0] = {
      ...modifiedMockDataSearchResponse[0],
      companyName: customCompanyName,
    };
  });
});

describe('Component tests for the search bar on the company search page', () => {
  it('Check if substrings of autocomplete entries are highlighted', { scrollBehavior: false }, () => {
    cy.mountWithPlugins(FrameworkDataSearchBar, {
      keycloak: minimalKeycloakMock({}),
    });
    cy.intercept('**/api/companies*', modifiedMockDataSearchResponse).as('searchCompany');
    cy.get('input[id=framework_data_search_bar_standard]').click({ force: true }).type(highlightedSubString);
    cy.wait('@searchCompany', { timeout: Cypress.env('short_timeout_in_ms') as number });
    cy.get('.p-autocomplete-item')
      .eq(0)
      .get("span[class='font-semibold']")
      .contains(highlightedSubString)
      .should('exist');
  });
});
