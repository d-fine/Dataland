// @ts-nocheck
import FrameworkDataSearchResults from '@/components/resources/frameworkDataSearch/FrameworkDataSearchResults.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type BasicCompanyInformation } from '@clients/backend';

let mockDataSearchResponse: Array<BasicCompanyInformation>;
before(function () {
  cy.fixture('DataSearchStoredCompanyMocks').then(function (jsonContent) {
    mockDataSearchResponse = jsonContent as Array<BasicCompanyInformation>;
  });
});

describe("Component tests for 'no result text' on the level of company search results", () => {
  const keycloakMock = minimalKeycloakMock({});

  it("Check that 'no result text' is not appearing in case of a successful company search", () => {
    cy.mountWithPlugins<typeof FrameworkDataSearchResults>(FrameworkDataSearchResults, {
      keycloak: keycloakMock,
    }).then((mounted) => {
      void mounted.wrapper.setProps({
        data: mockDataSearchResponse,
        rowsPerPage: 100,
      });
      cy.get('[data-test="DataSearchNoResultsText"]').should('not.exist');
    });
  });

  it("Check that the 'no result text' appears if company search is not successful", () => {
    cy.mountWithPlugins<typeof FrameworkDataSearchResults>(FrameworkDataSearchResults, {
      keycloak: keycloakMock,
    }).then(() => {
      cy.get('[data-test="DataSearchNoResultsText"]')
        .should('exist')
        .contains("We're sorry, but your search did not return any results.");
      cy.get('[data-test="DataSearchNoResultsText"]')
        .should('exist')
        .contains('Please double-check the spelling and filter settings!');
      cy.get('[data-test="DataSearchNoResultsText"]')
        .should('exist')
        .contains('It might be possible that the company you searched for does not exist on Dataland yet.');
    });
  });
});
