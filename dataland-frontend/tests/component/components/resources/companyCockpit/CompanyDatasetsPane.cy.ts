import CompanyDatasetsPane from '@/components/resources/companyCockpit/CompanyDatasetsPane.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum } from '@clients/backend';

describe('Component test for the company datasets pane (non-sourceability)', () => {
  // Valid UUID format required by isCompanyIdValid, used e.g. by the company-ownership check.
  const companyId = 'ad50bc6e-eb04-4bf6-9e70-a4b1c1c8e161';

  /**
   * Mounts CompanyDatasetsPane with the standard set of intercepts needed for it to mount cleanly,
   * plus a configurable mocked response for the non-sourceable dimensions search.
   * @param nonSourceableSearchResponse the mocked response for POST /api/non-sourceable/search
   */
  function mountComponentWithMocks(nonSourceableSearchResponse: object[]): void {
    cy.intercept('GET', `/api/companies/${companyId}/aggregated-framework-data-summary`, {}).as('getAggregatedSummary');
    cy.intercept('HEAD', `/community/company-ownership/${companyId}`, { statusCode: 200, body: {} }).as(
      'getCompanyOwnership'
    );
    cy.intercept('GET', '/documents/*', []).as('searchDocuments');
    cy.intercept('POST', '/api/non-sourceable/search', nonSourceableSearchResponse).as('postNonSourceableSearch');

    //@ts-ignore
    cy.mountWithPlugins(CompanyDatasetsPane, {
      keycloak: minimalKeycloakMock({}),
      props: { companyId },
    });

    cy.wait('@getAggregatedSummary');
    cy.wait('@getCompanyOwnership');
    cy.wait('@postNonSourceableSearch');
  }

  it('Shows the correct non-sourceable count on the correct framework tile, grouped separately per framework', () => {
    mountComponentWithMocks([
      { companyId, dataType: DataTypeEnum.Sfdr, reportingPeriod: '2023' },
      { companyId, dataType: DataTypeEnum.Sfdr, reportingPeriod: '2022' },
      { companyId, dataType: DataTypeEnum.NuclearAndGas, reportingPeriod: '2023' },
    ]);
    cy.get(`[data-test="${DataTypeEnum.Sfdr}-panel-non-sourceable-value"]`).should('have.text', '2');
    cy.get(`[data-test="${DataTypeEnum.NuclearAndGas}-panel-non-sourceable-value"]`).should('have.text', '1');
  });

  it('Does not show a non-sourceable hint on any tile when the response is empty', () => {
    mountComponentWithMocks([]);

    cy.get('[data-test="summaryPanels"]').find('.summary-panel__non-sourceable').should('not.exist');
  });
});
