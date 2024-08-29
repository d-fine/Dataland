import CreateAdditionalCompanyInformationDataset from '@/components/forms/CreateAdditionalCompanyInformationDataset.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component tests for the CreateAdditionalCompanyInformation dataset that test dependent fields', () => {
  /**
   * Picks the year 2024 in the datepicker
   */
  function pickDate(): void {
    cy.get('[data-test="reportingPeriod"]').click();
    cy.get('div.p-datepicker').find('span:contains("2024")').click();
  }

  /**
   * Pick fiscal year deviation "No Deviation" and check that no reports can be selected
   */
  function pickFiscalYearDeviationAndCheckThatNoReportsCanBeSelected(): void {
    cy.get('[data-test="dataQuality"').should('not.exist');
    cy.get('div[data-test="fiscalYearDeviation"] input[type="checkbox"][value="Deviation"]').check();
    cy.get('[data-test="dataQuality"').should('exist');
    cy.get('div[data-test="fiscalYearDeviation"] input[type="checkbox"][value="Deviation"]').uncheck();
    cy.get('[data-test="dataQuality"').should('not.exist');
    cy.get('div[data-test="fiscalYearDeviation"] input[type="checkbox"][value="NoDeviation"]').check();
    cy.get('[data-test="dataQuality"').should('exist');

    cy.get('div[data-test="dataReport"]').should('exist').click();
    cy.get('li:contains("None")').should('exist').click();
  }

  it('On the upload page, ensure that fiscalYearDeviation can be selected and deselected and page looks as expected', () => {
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
      pickFiscalYearDeviationAndCheckThatNoReportsCanBeSelected();
    });
  });
});
