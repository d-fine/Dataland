// @ts-nocheck
import CreateEuTaxonomyForFinancials from '@/components/forms/CreateEuTaxonomyForFinancials.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { noReportLabel } from '@/utils/DataSource';
import { DataTypeEnum } from '@clients/backend';

describe('Component tests for the Eu Taxonomy for financials that test dependent fields', () => {
  /**
   * Fills the required fields of the form which are always present
   */
  function fillDefaultRequiredFields(): void {
    cy.get('[data-test=assuranceDataSingleSelect]').find('.p-dropdown-trigger').click();
    cy.get('.p-dropdown-items').contains('None').click();

    cy.get('[data-test=companyReportsSingleSelect]').find('.p-dropdown-trigger').click();
    cy.get('.p-dropdown-items').contains(noReportLabel).click();
  }

  /**
   * Selects Asset Management as the financial service type
   */
  function selectAssetManagement(): void {
    cy.get('[data-test=MultiSelectfinancialServicesTypes]').click();
    cy.get('li.p-multiselect-item:contains(Asset Management)').click();
    cy.get('[data-test=addKpisButton]').click();
    cy.get('body').type('{esc}');
  }

  /**
   * Toggles all datapoints
   */
  function toggleAllDataPoints(): void {
    cy.get('[data-test=dataPointToggleButton]').should('have.length', 5);
    cy.get('[data-test=dataPointToggleButton]').each((toggleButton) => {
      cy.wrap(toggleButton).click();
    });
  }

  /**
   * Submits the form and validates success
   */
  function submitAndValidateSuccess(): void {
    cy.intercept(
      {
        method: 'POST',
        times: 1,
        url: `/api/data/${DataTypeEnum.EutaxonomyFinancials}*`,
      },
      {
        statusCode: 200,
      }
    ).as('postData');
    submitButton.clickButton();
    cy.wait('@postData');
    cy.get('.p-message-success').should('exist');
  }

  it('Upload an asset management only service successfully', () => {
    cy.mountWithPlugins(CreateEuTaxonomyForFinancials, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          companyID: 'dummy-id',
        };
      },
    }).then(() => {
      fillDefaultRequiredFields();
      selectAssetManagement();
      toggleAllDataPoints();
      submitAndValidateSuccess();
    });
  });
});
