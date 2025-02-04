// @ts-nocheck
import FrameworkDataSearchFilters from '@/components/resources/frameworkDataSearch/FrameworkDataSearchFilters.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type VueWrapper } from '@vue/test-utils';
import { assertDefined } from '@/utils/TypeScriptUtils';

describe('Component test for FrameworkDataSearchFilters', () => {
  it('Tests that the Search Filter Emits an event when a new item is selected', () => {
    const mockDistinctValues = {
      countryCodes: ['DE', 'CH'],
      sectors: ['DummySector', 'NotSelectedSector'],
    };
    cy.intercept('**/api/companies/meta-information', mockDistinctValues);
    cy.mountWithPlugins(FrameworkDataSearchFilters, {
      keycloak: minimalKeycloakMock({}),
    }).then((mounted) => {
      return cy.wrap(mounted.wrapper).as('vue');
    });

    cy.get('#country-filter').click();
    cy.get('span').contains('Germany').click();

    cy.get('#sector-filter').click();
    cy.get('span').contains('DummySector').click();

    // Ignored as TS does not understand that "vue" is not a JQuery Component but rather the whole wrapper

    // @ts-ignore
    cy.get('@vue').should((wrapper: VueWrapper<InstanceType<typeof FrameworkDataSearchFilters>>) => {
      const emittedCountryCodes = wrapper.emitted('update:selectedCountryCodes');
      expect(emittedCountryCodes).to.have.length;
      const emittedCountryCodesDefined = assertDefined(emittedCountryCodes);
      expect(emittedCountryCodesDefined[0][0]).to.deep.equal(['DE']);

      const emittedSectors = wrapper.emitted('update:selectedSectors');
      expect(emittedSectors).to.have.length;
      const emittedSectorsDefined = assertDefined(emittedSectors);
      expect(emittedSectorsDefined[0][0]).to.deep.equal(['DummySector']);
    });
  });
});
