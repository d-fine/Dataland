import DataPointFormWithToggle from '@/components/forms/parts/kpiSelection/DataPointFormWithToggle.vue';
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from '@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel';
import { selectItemFromDropdownByValue } from '@sharedUtils/Dropdown';
describe('Component tests for toggle data point', () => {
  it('On the upload page, ensure that data point can be hidden and shown and the data will be assigned accordingly', () => {
    //@ts-ignore
    cy.mountWithPlugins(DataPointFormWithToggle, {
      // @ts-ignore
      props: {
        name: 'tradingPortfolioInPercent',
        kpiInfoMappings: euTaxonomyKpiInfoMappings,
        kpiNameMappings: euTaxonomyKpiNameMappings,
      },
    }).then((mounted) => {
      cy.get('[data-test="value"]').should('be.visible').type('133');
      cy.get('[data-test="valueAsPercentageInSecondInputMode"]').should('not.exist');
      selectItemFromDropdownByValue(cy.get('[data-test="qualityValue"]'), 'Estimated');
      cy.wrap(mounted.component).its('currentQualityValue').should('eq', 'Estimated');
      cy.get('[data-test="dataPointToggleButton"]').click();
      cy.get('[data-test="value"]').should('not.exist');
      cy.get('[data-test="qualityValue"]').should('not.be.visible');
      cy.wrap(mounted.component).its('currentValue').should('eq', '');
      cy.wrap(mounted.component).its('currentQualityValue').should('eq', null);
      cy.get('[data-test="dataPointToggleButton"]').click();
      cy.get('[data-test="value"]').should('be.visible');
      cy.get('[data-test="qualityValue"]').should('be.visible');
      cy.wrap(mounted.component).its('currentValue').should('eq', '133');
      cy.wrap(mounted.component).its('currentQualityValue').should('eq', 'Estimated');
    });
  });

  it('Shows percentage and absolute inputs in second input mode and restores them after toggling', () => {
    //@ts-ignore
    cy.mountWithPlugins(DataPointFormWithToggle, {
      // @ts-ignore
      props: {
        name: 'tradingPortfolioInPercent',
        kpiInfoMappings: euTaxonomyKpiInfoMappings,
        kpiNameMappings: euTaxonomyKpiNameMappings,
        showSecondInput: true,
      },
    }).then((mounted) => {
      cy.get('[data-test="value"]').should('not.exist');
      cy.get('[data-test="valueAsPercentageInSecondInputMode"]').should('be.visible').type('12');
      cy.get('[data-test="valueAsAbsoluteInSecondInputMode"]').should('be.visible').type('34');
      cy.get('[data-test="dataPointToggleButton"]').click();
      cy.wrap(mounted.component).its('currentAmountValue').should('eq', '');
      cy.get('[data-test="dataPointToggleButton"]').click();
      cy.wrap(mounted.component).its('currentValue').should('eq', '12');
      cy.wrap(mounted.component).its('currentAmountValue').should('eq', '34');
    });
  });
});
