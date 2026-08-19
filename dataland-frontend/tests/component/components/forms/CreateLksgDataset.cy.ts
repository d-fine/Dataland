import CreateLksgDataset from '@/components/forms/CreateLksgDataset.vue';
import { type CompanyAssociatedDataLksgData } from '@clients/backend';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { UploadDocuments } from '@sharedUtils/components/UploadDocuments';

const mediumTimeoutInMs = Number(Cypress.expose('medium_timeout_in_ms') ?? 30000);

describe('Test YesNoBaseDataPointFormField for entries', () => {
  it('Filling out the form should work properly when adding and then removing a referenced document', () => {
    mountLksgCreateForm().then(() => {
      cy.get('[data-test="BaseDataPointFormFieldriskManagementSystem"] input[type="checkbox"][value="Yes"]').check();

      new UploadDocuments('riskManagementSystem').selectDummyFile('riskManagementSystemDocument', 1);

      cy.get('[data-test="BaseDataPointFormFieldriskManagementSystem"] button[data-test="files-to-upload-remove"]', {
        timeout: mediumTimeoutInMs,
      }).should('exist');

      cy.get('[data-test="BaseDataPointFormFieldriskManagementSystem"] input[type="checkbox"][value="No"]').check();

      cy.get(
        '[data-test="BaseDataPointFormFieldriskManagementSystem"] button[data-test="files-to-upload-remove"]'
      ).should('not.exist');
    });
  });

  it('Filling out the form should work properly when selecting a subcontracting company country and industry', () => {
    mountLksgCreateForm().then(() => {
      fillDataDate();
      cy.get('[data-test="manufacturingCompany"] input[type="checkbox"][value="Yes"]').check();
      cy.get('[data-test="productionViaSubcontracting"] input[type="checkbox"][value="Yes"]').check();

      cy.get('[data-test="subcontractingCompaniesCountries"]', { timeout: mediumTimeoutInMs }).within(() => {
        cy.get('[data-pc-name="multiselect"]').should('be.visible').click();
      });
      cy.get('[data-pc-name="multiselect"]')
        .get('[data-pc-section="list"]')
        .find('li')
        .get('[aria-label="Albania (AL)"]')
        .should('contain', 'Albania (AL)')
        .click();
      cy.get('body').type('{esc}');

      cy.get('h5:contains("Subcontracting Companies Industries in Albania")').should('exist');

      cy.get('[data-test="NaceCodeSelectorInput"]').should('be.visible').click().type('01.11');
      cy.get('[data-test="NaceCodeSelectorTree"]')
        .find('li')
        .should('have.length', 4)
        .eq(3)
        .should('contain', 'Growing of cereals (except rice), leguminous crops and oil seeds')
        .get('[data-pc-section="label"]')
        .get('[data-test="NaceCodeSelectorCheckbox"]')
        .last()
        .click();

      cy.get('h5:contains("Subcontracting Companies Industries in Albania")')
        .parents('.form-field')
        .first()
        .find('.d-nace-chipview')
        .children()
        .should('have.length', 1);

      cy.intercept('**/api/data/lksg*', (request) => {
        const body = request.body as CompanyAssociatedDataLksgData;
        expect(body.data.general?.productionSpecific?.subcontractingCompaniesCountries).to.deep.equal({
          AL: ['01.11'],
        });
        request.reply(200);
      }).as('send');
      submitButton.clickButton();
      cy.wait('@send');
    });
  });
});

/**
 * Fills the required "Data Date" field with a date in the future so that the form can be submitted
 */
function fillDataDate(): void {
  cy.get('[data-test="dataDate"] button').should('have.class', 'p-datepicker-dropdown').click();
  cy.get('.p-datepicker-header').find('button[aria-label="Next Month"]').click();
  cy.get('.p-datepicker-day-view').find('span:contains("11")').click();
}

/**
 * Function to mount an empty lksg upload form that is ready to be filled out interactively
 * @returns the mounted component
 */
function mountLksgCreateForm(): Cypress.Chainable {
  return getMountingFunction({ keycloak: minimalKeycloakMock() })(CreateLksgDataset, {
    props: {
      companyID: 'company-id',
    },
  });
}
