import PortfolioDetailsBase from '@/components/resources/portfolio/PortfolioDetailsBase.vue';
import { type EnrichedPortfolio } from '@clients/userservice';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { DataTypeEnum } from '@clients/backend';

describe('Component test for the portfolio details base component (non-sourceability)', () => {
  let portfolioFixture: EnrichedPortfolio;

  before(function () {
    cy.fixture('enrichedPortfolio.json').then(function (jsonContent) {
      portfolioFixture = {
        ...(jsonContent as EnrichedPortfolio),
        sharedUserIds: [] as unknown as Set<string>,
      } as EnrichedPortfolio;
    });
  });

  it('Merges non-sourceable periods into the correct cell (struck through, sorted) while leaving other cells unaffected', () => {
    const apricotCompanyId = '55211efc-5502-430e-89b5-afed16a44407';

    cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolioFixture).as('getEnrichedPortfolio');
    cy.intercept('POST', '/api/non-sourceable/search/grouped', {
      [apricotCompanyId]: {
        [DataTypeEnum.Sfdr]: [{ companyId: apricotCompanyId, dataType: DataTypeEnum.Sfdr, reportingPeriod: '2022' }],
      },
    }).as('postNonSourceableSearchGrouped');

    //@ts-ignore
    cy.mountWithPlugins(PortfolioDetailsBase, {
      keycloak: minimalKeycloakMock({}),
      props: { portfolioId: portfolioFixture.portfolioId },
    });

    cy.wait('@getEnrichedPortfolio');
    cy.wait('@postNonSourceableSearchGrouped');

    // Apricot Inc. (row 1) / SFDR (column 4): real period 2024 merged with non-sourceable 2022, sorted ascending.
    const apricotSfdrCell = 'table tr:first-child td:nth-child(4)';
    cy.get(apricotSfdrCell).should('contain.text', '2022, 2024');
    cy.get(apricotSfdrCell).find('.non-sourceable-year').should('have.length', 1).and('have.text', '2022');

    // Banana LLC (row 2) / EU Taxonomy Financials (column 5): no non-sourceable data mocked for it,
    // so it must show its real period without any strikethrough.
    const bananaEutaxonomyFinancialsCell = 'table tr:nth-child(2) td:nth-child(5)';
    cy.get(bananaEutaxonomyFinancialsCell).should('contain.text', '2023');
    cy.get(bananaEutaxonomyFinancialsCell).find('.non-sourceable-year').should('not.exist');
  });

  describe('Proxy for column', () => {
    const apricotId = '55211efc-5502-430e-89b5-afed16a44407';
    const bananaId = '6d9fe2f5-b46d-4e87-8ad4-45361584889a';
    const externalId = 'external-company-id';

    /**
     * Mounts the component with the given portfolio.
     * @param portfolio the portfolio returned by the backend
     */
    function mountWith(portfolio: EnrichedPortfolio): void {
      cy.intercept('**/users/portfolios/*/enriched-portfolio', portfolio).as('getEnrichedPortfolio');
      cy.intercept('POST', '/api/non-sourceable/search/grouped', {});
      //@ts-ignore
      cy.mountWithPlugins(PortfolioDetailsBase, {
        keycloak: minimalKeycloakMock({}),
        props: { portfolioId: portfolio.portfolioId },
      });
      cy.wait('@getEnrichedPortfolio');
    }

    it('Hides the column if there are no proxy replacements', () => {
      mountWith({ ...portfolioFixture, proxyCompanyReplacements: [] });
      cy.get('table').should('be.visible');
      cy.get('th').contains('Proxy for').should('not.exist');
    });

    it('Shows grouped proxied names, linking all proxied companies', () => {
      cy.intercept('GET', `**/companies/${externalId}/info`, { companyName: 'External Corp' }).as('getInfo');
      mountWith({
        ...portfolioFixture,
        proxyCompanyReplacements: [
          { proxyCompanyId: apricotId, proxiedCompanyId: bananaId, userId: 'u', timestamp: 1 },
          { proxyCompanyId: apricotId, proxiedCompanyId: externalId, userId: 'u', timestamp: 1 },
        ],
      } as EnrichedPortfolio);
      cy.wait('@getInfo');
      cy.get('th').contains('Proxy for').should('exist');
      const cell = 'table tr:first-child td:nth-child(2)';
      cy.get(cell).should('contain.text', 'Banana LLC').and('contain.text', 'External Corp');
      cy.get(cell)
        .find('[data-test="proxied-company-link"]')
        .should('have.length', 2)
        .and('contain.text', 'Banana LLC')
        .and('contain.text', 'External Corp');
      cy.get(cell).find(`a[href="/companies/${externalId}"]`).should('contain.text', 'External Corp');
      cy.get(cell).find('[data-test="proxied-company-text"]').should('not.exist');
      cy.get('table tr:nth-child(2) td:nth-child(2)').should('have.text', '');
    });
  });
});
