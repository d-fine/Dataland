import FrameworkSummaryPanel from '@/components/resources/companyCockpit/FrameworkSummaryPanel.vue';
import { DataTypeEnum } from '@clients/backend';

describe('Component test for the framework summary panel (non-sourceability)', () => {
  const companyId = 'ad50bc6e-eb04-4bf6-9e70-a4b1c1c8e161';
  const framework = DataTypeEnum.Sfdr;

  /**
   * Mounts the FrameworkSummaryPanel component with standard companyId/framework props, plus the
   * given non-sourceability related props.
   * @param props the non-sourceability related props to mount the component with
   */
  function mountComponent(props: {
    numberOfNonSourceableReportingPeriods?: number | null;
    nonSourceableReportingPeriods?: string[] | null;
  }): void {
    //@ts-ignore
    cy.mountWithPlugins(FrameworkSummaryPanel, {
      props: {
        companyId,
        framework,
        ...props,
      },
    });
  }

  it('Does not show a non-sourceable info block when there are no non-sourceable periods', () => {
    mountComponent({ numberOfNonSourceableReportingPeriods: 0 });

    cy.get(`[data-test="${framework}-panel-non-sourceable-value"]`).should('not.exist');
  });

  it('Shows singular wording for exactly one non-sourceable period', () => {
    mountComponent({
      numberOfNonSourceableReportingPeriods: 1,
      nonSourceableReportingPeriods: ['2023'],
    });

    cy.get(`[data-test="${framework}-panel-non-sourceable-value"]`).should('have.text', '1');
    cy.get(`[data-test="${framework}-panel-non-sourceable-value"]`)
      .parent()
      .should('contain.text', 'non-sourceable Period')
      .and('not.contain.text', 'non-sourceable Periods');
  });

  it('Shows plural wording for multiple non-sourceable periods', () => {
    mountComponent({
      numberOfNonSourceableReportingPeriods: 3,
      nonSourceableReportingPeriods: ['2021', '2022', '2023'],
    });

    cy.get(`[data-test="${framework}-panel-non-sourceable-value"]`).should('have.text', '3');
    cy.get(`[data-test="${framework}-panel-non-sourceable-value"]`)
      .parent()
      .should('contain.text', 'non-sourceable Periods');
  });

  it('Shows the correct tooltip content when hovering over the info icon', () => {
    mountComponent({
      numberOfNonSourceableReportingPeriods: 2,
      nonSourceableReportingPeriods: ['2022', '2023'],
    });

    cy.get(`[data-test="${framework}-non-sourceable-info-icon"]`).trigger('mouseenter', 'center');
    cy.get('.p-tooltip')
      .should('be.visible')
      .should('contain.text', 'The following years are non-sourceable:')
      .and('contain.text', '2022, 2023');
  });
});
