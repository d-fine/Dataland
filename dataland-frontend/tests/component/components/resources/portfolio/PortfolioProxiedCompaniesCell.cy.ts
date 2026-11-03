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
    { companyId: 'id0', companyName: 'Company 0' },
    { companyId: 'id1', companyName: 'Company 1' },
    { companyId: 'id2', companyName: 'Company 2' },
    { companyId: 'id3', companyName: 'Company 3' },
    { companyId: 'id4', companyName: 'Company 4' },
    { companyId: 'id5', companyName: 'Company 5' },
  ];

  it('Shows the names of all companies', () => {
    mountComponent(companies);
    companies.forEach((company) => {
      cy.get('[data-test="proxied-companies-cell"]').should('contain.text', company.companyName);
    });
  });

  it('Renders all companies as links and emits navigate when clicking a link', () => {
    mountComponent(companies).then(({ wrapper }) => {
      cy.get('[data-test="proxied-company-link"]').should('have.length', 6);
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
