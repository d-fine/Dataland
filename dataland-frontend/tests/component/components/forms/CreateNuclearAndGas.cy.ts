import CreateNuclearAndGasDataset from '@/components/forms/CreateNuclearAndGasDataset.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import DataPointFormWithToggle from '@/components/forms/parts/kpiSelection/DataPointFormWithToggle.vue';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component tests for the Nuclear and Gas that test dependent fields', () => {
  /**
   * this method fills and checks the general section
   */
  function fillAndValidateGeneralSection(): void {
    cy.get('div[data-test="nuclearEnergyRelatedActivitiesSection426"] input[type="checkbox"][value="Yes"]').check();
    cy.get('div[data-test="nuclearEnergyRelatedActivitiesSection427"] input[type="checkbox"][value="Yes"]').check();
    cy.get('div[data-test="nuclearEnergyRelatedActivitiesSection428"] input[type="checkbox"][value="Yes"]').check();
    cy.get('div[data-test="fossilGasRelatedActivitiesSection429"] input[type="checkbox"][value="Yes"]').check();
    cy.get('div[data-test="fossilGasRelatedActivitiesSection430"] input[type="checkbox"][value="Yes"]').check();
    cy.get('div[data-test="fossilGasRelatedActivitiesSection430"] input[type="checkbox"][value="Yes"]').check();
  }

  /**
   * this method fills and checks the Taxonomy-aligned (denominator) section
   */
  function fillAndValidateOtherSections(): void {
    const subsections = ['NAndG426', 'NAndG427', 'NAndG428', 'NAndG429', 'NAndG430', 'NAndG431', 'OtherActivities', ''];

    cy.get(
      `div[data-test="nuclearAndGasTaxonomyAlignedRevenueDenominator"] div[data-test="toggleDataPointWrapper"] div[data-test="dataPointToggleButton"]`
    )
      .should('exist')
      .click();
    subsections.forEach((subsection) => {
      cy.get(
        `div[data-test="nuclearAndGasTaxonomyAlignedRevenueDenominator"] div[data-test="taxonomyAlignedShareDenominator${subsection}"] input[name="mitigationAndAdaptation"]`
      )
        .clear()
        .type('12');
      cy.get(
        `div[data-test="nuclearAndGasTaxonomyAlignedRevenueDenominator"] div[data-test="taxonomyAlignedShareDenominator${subsection}"] input[name="mitigation"]`
      )
        .clear()
        .type('5');
      cy.get(
        `div[data-test="nuclearAndGasTaxonomyAlignedRevenueDenominator"] div[data-test="taxonomyAlignedShareDenominator${subsection}"] input[name="adaptation"]`
      )
        .clear()
        .type('8');
    });

    cy.get(
      `div[data-test="nuclearAndGasTaxonomyNonEligibleRevenue"] div[data-test="toggleDataPointWrapper"] div[data-test="dataPointToggleButton"]`
    )
      .should('exist')
      .click();
    subsections.forEach((subsection) => {
      cy.get(
        `div[data-test="nuclearAndGasTaxonomyNonEligibleRevenue"] div[data-test="taxonomyNonEligibleShare${subsection}"] input[name="taxonomyNonEligibleShare${subsection}"]`
      )
        .clear()
        .type('15');
    });
  }

  it('Open upload page, fill out and validate the upload form, except for new activities', () => {
    cy.stub(DataPointFormWithToggle);
    getMountingFunction({
      keycloak: minimalKeycloakMock(),
      // Ignored, as overwriting the data function is not safe anyway
      // @ts-ignore
    })(CreateNuclearAndGasDataset, {
      data() {
        return {};
      },
    }).then(() => {
      fillAndValidateGeneralSection();
      fillAndValidateOtherSections();
    });
  });
});
