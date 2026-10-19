import PortfolioProxiedCompaniesCell, {
  type ProxiedCompany,
} from '@/components/resources/portfolio/PortfolioProxiedCompaniesCell.vue';

describe('Component test for the portfolio proxied companies cell', () => {
  /**
   * Mounts the cell with the given companies.
   * @param companies the proxied companies
   */
  function mountComponent(
    companies: ProxiedCompany[]
  ): Cypress.Chainable<{ wrapper: { emitted: () => Record<string, unknown[]> } }> {
    //@ts-ignore
    return cy.mountWithPlugins(PortfolioProxiedCompaniesCell, { props: { companies } });
  }

  it('Shows a short list fully and without tooltip', () => {
    mountComponent([
      { companyId: 'a', companyName: 'Alpha', linkable: true },
      { companyId: 'b', companyName: 'Beta', linkable: false },
    ]);
    cy.get('[data-test="proxied-companies-cell"]').should('contain.text', 'Alpha').and('contain.text', 'Beta');
    cy.get('[data-test="proxied-companies-cell"]').should('not.contain.text', '…');
    cy.get('[data-test="proxied-companies-cell"]').trigger('mouseenter', 'center');
    cy.get('.p-tooltip').should('not.exist');
  });

  it('Renders linkable companies as links and others as plain text, and emits navigate', () => {
    mountComponent([
      { companyId: 'a', companyName: 'Alpha', linkable: true },
      { companyId: 'b', companyName: 'Beta', linkable: false },
    ]).then(({ wrapper }) => {
      cy.get('[data-test="proxied-company-link"]').should('have.length', 1).click();
      cy.get('[data-test="proxied-company-text"]')
        .should('have.length', 1)
        .then(() => {
          expect(wrapper.emitted()).to.have.property('navigate');
          expect(wrapper.emitted().navigate[0]).to.deep.equal(['a']);
        });
    });
  });

  it('Shows all companies of a long list as clickable links without truncation or tooltip', () => {
    const companies: ProxiedCompany[] = Array.from({ length: 6 }, (_, i) => ({
      companyId: `id${i}`,
      companyName: `Company number ${i}`,
      linkable: true,
    }));
    const fullText = companies.map((c) => c.companyName).join(', ');
    expect(fullText.length).to.be.greaterThan(60);
    mountComponent(companies).then(({ wrapper }) => {
      cy.get('[data-test="proxied-companies-cell"]').should('contain.text', 'Company number 5');
      cy.get('[data-test="proxied-companies-cell"]').should('not.contain.text', '…');
      cy.get('[data-test="proxied-company-link"]').should('have.length', 6);
      cy.get('[data-test="proxied-companies-cell"]').trigger('mouseenter', 'center');
      cy.get('.p-tooltip').should('not.exist');
      cy.get('[data-test="proxied-company-link"]')
        .last()
        .click()
        .then(() => {
          expect(wrapper.emitted().navigate[0]).to.deep.equal(['id5']);
        });
    });
  });
});
