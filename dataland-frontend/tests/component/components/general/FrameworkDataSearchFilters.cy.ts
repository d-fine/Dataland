import FrameworkDataSearchFilters from '@/components/resources/frameworkDataSearch/FrameworkDataSearchFilters.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type VueWrapper } from '@vue/test-utils';
import { assertDefined } from '@/utils/TypeScriptUtils';

describe('Component test for FrameworkDataSearchFilters', () => {
  it('Tests that the Search Filter Emits an event when a new item is selected', () => {
    const mockDistinctValues = {
      countryCodes: ['DE', 'CH'],
      sectors: ['DummySector', 'NotSelectedSector'],
      reportingPeriods: ['2023', '2024', '2025'],
    };
    cy.intercept('**/api/companies/meta-information', mockDistinctValues);
    //@ts-ignore
    cy.mountWithPlugins(FrameworkDataSearchFilters, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      return cy.wrap(mounted.wrapper).as('vue');
    });

    cy.get('[data-test="reset-filter"]').click();
    cy.get('[data-test="frameworkDataSearchDropdownFilterSector"]').click();
    cy.get('span').contains('DummySector').click();
    cy.get('.p-multiselect-overlay').invoke('hide');

    cy.get('#country-filter').click();
    cy.get('span').contains('Germany').click();

    // Ignored as TS does not understand that "vue" is not a JQuery Component but rather the whole wrapper
    // @ts-ignore
    cy.get('@vue').should((wrapper: VueWrapper<InstanceType<typeof FrameworkDataSearchFilters>>) => {
      const emittedCountryCodes = wrapper.emitted('update:selectedCountryCodes');
      expect(emittedCountryCodes).to.have.length.greaterThan(0);
      const emittedCountryCodesDefined = assertDefined(emittedCountryCodes);
      expect(emittedCountryCodesDefined[emittedCountryCodes!.length - 1][0]).to.deep.equal(['DE']);

      const emittedSectors = wrapper.emitted('update:selectedSectors');
      expect(emittedSectors).to.have.length.greaterThan(0);
      const emittedSectorsDefined = assertDefined(emittedSectors);
      expect(emittedSectorsDefined[emittedSectors!.length - 1][0]).to.deep.equal(['DummySector']);
    });
  });

  it('Tests that the reporting period filter starts unselected and lists available periods', () => {
    const mockDistinctValues = {
      countryCodes: ['DE', 'CH'],
      sectors: ['DummySector', 'NotSelectedSector'],
      reportingPeriods: ['2023', '2024', '2025'],
    };
    cy.intercept('**/api/companies/meta-information', mockDistinctValues);
    //@ts-ignore
    cy.mountWithPlugins(FrameworkDataSearchFilters, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      return cy.wrap(mounted.wrapper).as('vue');
    });

    cy.get('#reporting-period-filter').should('contain.text', 'Reporting Period');

    cy.get('#reporting-period-filter').click();
    cy.get('li').contains('2025').should('exist');
    cy.get('li').contains('2024').should('exist');
    cy.get('li').contains('2023').should('exist');
  });

  it('Tests that selecting and clearing the reporting period filter emits the correct events', () => {
    const mockDistinctValues = {
      countryCodes: ['DE', 'CH'],
      sectors: ['DummySector', 'NotSelectedSector'],
      reportingPeriods: ['2023', '2024', '2025'],
    };
    cy.intercept('**/api/companies/meta-information', mockDistinctValues);
    //@ts-ignore
    cy.mountWithPlugins(FrameworkDataSearchFilters, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      return cy.wrap(mounted.wrapper).as('vue');
    });

    cy.get('#reporting-period-filter').click();
    cy.get('span').contains('2023').click();
    cy.get('.p-multiselect-overlay').invoke('hide');

    // @ts-ignore
    cy.get('@vue').should((wrapper: VueWrapper<InstanceType<typeof FrameworkDataSearchFilters>>) => {
      const emittedReportingPeriods = wrapper.emitted('update:selectedReportingPeriods');
      expect(emittedReportingPeriods).to.have.length.greaterThan(0);
      const emittedReportingPeriodsDefined = assertDefined(emittedReportingPeriods);
      expect(emittedReportingPeriodsDefined[emittedReportingPeriods!.length - 1][0]).to.deep.equal(['2023']);
    });

    cy.get('[data-test="reset-filter"]').click();

    // @ts-ignore
    cy.get('@vue').should((wrapper: VueWrapper<InstanceType<typeof FrameworkDataSearchFilters>>) => {
      const emittedReportingPeriods = wrapper.emitted('update:selectedReportingPeriods');
      const emittedReportingPeriodsDefined = assertDefined(emittedReportingPeriods);
      expect(emittedReportingPeriodsDefined[emittedReportingPeriods!.length - 1][0]).to.deep.equal([]);
    });
  });
});
