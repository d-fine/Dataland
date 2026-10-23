import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { getMountingFunction } from '@ct/testUtils/Mount';
import CompaniesOnlySearchBar from '@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue';
import { type CompanyIdAndName } from '@clients/backend';

/**
 * Types the input into the search bar
 * @param input to type
 */
function typeIntoSearchBar(input: string): void {
  cy.get(`input[id="company_search_bar_standard"]`).type(input);
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
  cy.get('div.p-autocomplete-overlay').should(isPanelExpectedToExist ? 'exist' : 'not.exist');
}

describe('Component test for companies search bar', () => {
  it('No search occurs and a warning is displayed if search string is too short', () => {
    const mockSearchResults: CompanyIdAndName[] = [
      { companyName: 'abc123', companyId: crypto.randomUUID() },
      { companyName: 'abc456', companyId: crypto.randomUUID() },
      { companyName: 'abc789', companyId: crypto.randomUUID() },
    ];

    getMountingFunction({ keycloak: minimalKeycloakMock() })(CompaniesOnlySearchBar);
    validateSearchStringWarning(false);
    validateAutocompletePanel(false);

    typeIntoSearchBar('a');
    validateSearchStringWarning(true);
    validateAutocompletePanel(false);

    typeIntoSearchBar('b');
    validateSearchStringWarning(true);
    validateAutocompletePanel(false);

    cy.intercept(`**/companies/names?searchString=abc&resultLimit=100`, mockSearchResults).as('searchForAbc');
    typeIntoSearchBar('c');
    validateSearchStringWarning(false);
    cy.wait('@searchForAbc');
    validateAutocompletePanel(true);

    cy.get(`input[id="company_search_bar_standard"]`).clear();
    validateSearchStringWarning(false);
    validateAutocompletePanel(false);
  });

  it('Emits selectCompany with the selected company when a suggestion is clicked', () => {
    const mockSearchResults: CompanyIdAndName[] = [
      { companyName: 'abc123', companyId: crypto.randomUUID() },
      { companyName: 'abc456', companyId: crypto.randomUUID() },
    ];
    cy.intercept(`**/companies/names?searchString=abc&resultLimit=100`, mockSearchResults).as('searchForAbc');

    getMountingFunction({ keycloak: minimalKeycloakMock() })(CompaniesOnlySearchBar).then(({ wrapper }) => {
      typeIntoSearchBar('abc');
      cy.wait('@searchForAbc');
      cy.get('.p-autocomplete-option').first().click();
      cy.wrap(wrapper).should((vueWrapper) => {
        expect(vueWrapper.emitted('selectCompany')).to.have.length(1);
        expect(vueWrapper.emitted('selectCompany')![0]).to.deep.equal([mockSearchResults[0]]);
      });
    });
  });
});
