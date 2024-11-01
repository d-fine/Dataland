import FrameworkDataSearchBar from '@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type BasicCompanyInformation } from '@clients/backend';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { faker } from '@faker-js/faker';

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

/**
 * Types the input into the search bar
 * @param input to type
 */
function typeIntoSearchBar(input: string): void {
  cy.get('input[id=framework_data_search_bar_standard]').type(input);
}

/**
 * Checks if the warning is there or is not there, based on the boolean passed to the function.
 * @param isWarningExpectedToExist decides whether the warning is expected to be displayed or not
 */
function validateSearchStringWarning(isWarningExpectedToExist: boolean): void {
  cy.contains('span', 'Please type at least 3 characters').should(isWarningExpectedToExist ? 'exist' : 'not.exist');
}

/**
 * Checks if the autocomplete panel is there or is not there, based on the boolean passed to the function.
 * @param isPanelExpectedToExist decides whether the panel is expected to be displayed or not
 */
function validateAutocompletePanel(isPanelExpectedToExist: boolean): void {
  cy.get('div.p-autocomplete-panel').should(isPanelExpectedToExist ? 'exist' : 'not.exist');
}

describe('Component tests for the search bar on the company search page', () => {
  it('Check if substrings of autocomplete entries are highlighted', { scrollBehavior: false }, () => {
    getMountingFunction({ keycloak: minimalKeycloakMock() })(FrameworkDataSearchBar);
    cy.intercept('**/api/companies*', modifiedMockDataSearchResponse).as('searchCompany');
    typeIntoSearchBar(highlightedSubString);
    cy.wait('@searchCompany', { timeout: Cypress.env('short_timeout_in_ms') as number });
    validateAutocompletePanel(true);
    cy.get('.p-autocomplete-item')
      .eq(0)
      .get("span[class='font-semibold']")
      .contains(highlightedSubString)
      .should('exist');
  });

  it('No search occurs and a warning is displayed if search string is too short', () => {
    const mockSearchResultsWithAbcInName = modifiedMockDataSearchResponse.map((searchResult) => {
      searchResult.companyName = `abc${faker.company.name()}`;
      return searchResult;
    });

    getMountingFunction({ keycloak: minimalKeycloakMock() })(FrameworkDataSearchBar);
    validateSearchStringWarning(false);
    validateAutocompletePanel(false);

    typeIntoSearchBar('a');
    validateSearchStringWarning(true);
    validateAutocompletePanel(false);

    typeIntoSearchBar('b');
    validateSearchStringWarning(true);
    validateAutocompletePanel(false);

    cy.intercept(`**/companies?searchString=abc**&chunkSize=3&chunkIndex=0`, mockSearchResultsWithAbcInName).as(
      'fetchAutoCompleteSuggestionsForAbc'
    );
    typeIntoSearchBar('c');
    validateSearchStringWarning(false);
    validateAutocompletePanel(true);

    cy.get(`input[id="framework_data_search_bar_standard"]`).clear();
    validateSearchStringWarning(false);
    validateAutocompletePanel(false);
  });
});
