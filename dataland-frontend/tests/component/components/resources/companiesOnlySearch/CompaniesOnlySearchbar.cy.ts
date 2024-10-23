import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { getMountingFunction } from '@ct/testUtils/Mount';
import CompaniesOnlySearchBar from '@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue';

describe('Component test for ClaimOwnershipPanel', () => {
  beforeEach(() => {
    const mockApiResponse = { status: 200 };
    cy.intercept('**/company-ownership/*', mockApiResponse);
  });

  it('No search occurs and warning is displayed if search string is too short', () => {
    getMountingFunction({ keycloak: minimalKeycloakMock() })(CompaniesOnlySearchBar);
    cy.contains('span', 'Please type at least 3 characters').should('not.exist');

    cy.get(`input[id="company_search_bar_standard"]`).type('a');
    cy.contains('span', 'Please type at least 3 characters').should('exist');

    cy.get(`input[id="company_search_bar_standard"]`).type('b');
    cy.contains('span', 'Please type at least 3 characters').should('exist');

    cy.intercept(`**/companies/names?searchString=abc&resultLimit=100`, []).as('searchForAbc');
    cy.get(`input[id="company_search_bar_standard"]`).type('c');
    cy.contains('span', 'Please type at least 3 characters').should('not.exist');
    cy.wait('@searchForAbc');

    cy.get(`input[id="company_search_bar_standard"]`).clear();
    cy.contains('span', 'Please type at least 3 characters').should('not.exist');
  });
});
