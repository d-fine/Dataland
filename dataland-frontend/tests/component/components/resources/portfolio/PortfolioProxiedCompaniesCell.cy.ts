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

  const companies: ProxiedCompany[] = [
    { companyId: 'id0', companyName: 'Company 0', linkable: true },
    { companyId: 'id1', companyName: 'Company 1', linkable: false },
    { companyId: 'id2', companyName: 'Company 2', linkable: true },
    { companyId: 'id3', companyName: 'Company 3', linkable: true },
    { companyId: 'id4', companyName: 'Company 4', linkable: false },
    { companyId: 'id5', companyName: 'Company 5', linkable: true },
  ];

  it('Shows the names of all companies', () => {
    mountComponent(companies);
    companies.forEach((company) => {
      cy.get('[data-test="proxied-companies-cell"]').should('contain.text', company.companyName);
    });
  });

  it('Renders linkable companies as links and others as plain text, and emits navigate when clicking a link', () => {
    mountComponent(companies).then(({ wrapper }) => {
      cy.get('[data-test="proxied-company-link"]').should('have.length', 4);
      cy.get('[data-test="proxied-company-text"]').should('have.length', 2);
      cy.get('[data-test="proxied-company-link"]')
        .last()
        .click()
        .then(() => {
          expect(wrapper.emitted().navigate).to.have.length(1);
          expect(wrapper.emitted().navigate[0]).to.deep.equal(['id5']);
        });
    });
  });
});
