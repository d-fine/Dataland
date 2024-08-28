import CreateAdditionalCompanyInformationDataset from '@/components/forms/CreateAdditionalCompanyInformationDataset.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component tests for the CreateAdditionalCompanyInformation dataset that test dependent fields', () => {
  /**
   * Picks the the year 2024 in the datepicker
   */
  function pickDate(): void {
    cy.get('[data-test="reportingPeriod"]').click();
    cy.get('div.p-datepicker').find('span:contains("2024")').click();
  }

  /**
   * Pick fiscal year deviation "No Deviation"
   */
  function pickFiscalYearDeviation(): void {
    cy.get('[data-test="dataQuality"').should('not.exist');
    cy.get('div[data-test="fiscalYearDeviation"] input[type="checkbox"][value="Deviation"]').check();
    cy.get('[data-test="dataQuality"').should('exist');
    cy.get('div[data-test="fiscalYearDeviation"] input[type="checkbox"][value="Deviation"]').uncheck();
    cy.get('[data-test="dataQuality"').should('not.exist');
    cy.get('div[data-test="fiscalYearDeviation"] input[type="checkbox"][value="NoDeviation"]').check();
    cy.get('[data-test="dataQuality"').should('exist');
  }

  it('On the upload page, ensure that sectors can be selected and deselected and the submit looks as expected', () => {
    getMountingFunction({
      keycloak: minimalKeycloakMock(),
      dialogOptions: {
        mountWithDialog: true,
        propsToPassToTheMountedComponent: {
          companyID: 'company-id-does-not-matter-in-this-test',
        },
      },
    })(CreateAdditionalCompanyInformationDataset).then(() => {
      cy.get('[name="reportingPeriod"]').should('be.hidden').should('contain.value', '0');
      pickDate();
      cy.get('[name="reportingPeriod"]').should('contain.value', '2024');
      pickFiscalYearDeviation();
    });
  });
});
