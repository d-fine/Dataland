import PortfolioReportingPeriodsCell, {
  type ReportingPeriodEntry,
} from '@/components/resources/portfolio/PortfolioReportingPeriodsCell.vue';

describe('Component test for the portfolio reporting periods cell (non-sourceability)', () => {
  /**
   * Mounts the PortfolioReportingPeriodsCell component with the given periods and clickable flag.
   * @param periods the reporting periods to display
   * @param clickable whether the cell should be rendered as a clickable button
   */
  function mountComponent(
    periods: ReportingPeriodEntry[],
    clickable: boolean
  ): Cypress.Chainable<{ wrapper: { emitted: () => Record<string, unknown[]> } }> {
    //@ts-ignore
    return cy.mountWithPlugins(PortfolioReportingPeriodsCell, {
      props: { periods, clickable },
    });
  }

  it('Shows "No data available" when there are no periods (non-clickable)', () => {
    mountComponent([], false);

    cy.contains('No data available').should('exist');
    cy.get('.non-sourceable-year').should('not.exist');
  });

  it('Shows periods comma-separated without strikethrough when none are non-sourceable', () => {
    mountComponent(
      [
        { year: '2022', nonSourceable: false },
        { year: '2023', nonSourceable: false },
      ],
      false
    );

    cy.get('span').should('contain.text', '2022, 2023');
    cy.get('.non-sourceable-year').should('not.exist');
  });

  it('Shows non-sourceable periods struck through and with a tooltip', () => {
    mountComponent(
      [
        { year: '2022', nonSourceable: true },
        { year: '2023', nonSourceable: false },
      ],
      false
    );

    cy.get('.non-sourceable-year').should('have.length', 1).and('have.text', '2022');
    cy.get('span').first().trigger('mouseenter', 'center');
    cy.get('.p-tooltip')
      .should('be.visible')
      .and('contain.text', 'If a year number is strikethrough, the report of this year is non-sourceable');
  });

  it('Renders as a clickable button, still shows strikethrough/tooltip, and emits navigate on click', () => {
    mountComponent(
      [
        { year: '2022', nonSourceable: true },
        { year: '2023', nonSourceable: false },
      ],
      true
    ).then(({ wrapper }) => {
      cy.get('button').should('exist');
      cy.get('.non-sourceable-year').should('have.length', 1).and('have.text', '2022');

      cy.get('button')
        .click()
        .then(() => {
          cy.wrap(wrapper.emitted()).should('have.property', 'navigate');
        });
    });
  });
});
