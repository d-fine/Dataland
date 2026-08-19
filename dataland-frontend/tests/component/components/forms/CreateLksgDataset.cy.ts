import CreateLksgDataset from '@/components/forms/CreateLksgDataset.vue';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { type CompanyAssociatedDataLksgData, type LksgData } from '@clients/backend';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { getMountingFunction } from '@ct/testUtils/Mount.ts';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

const mediumTimeoutInMs = Number(Cypress.expose('medium_timeout_in_ms') ?? 30000);

describe('Test YesNoBaseDataPointFormField for entries', () => {
  let preparedFixtures: Array<FixtureData<LksgData>>;
  before(() => {
    cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
    });
  });

  it('Prefilled form should work properly when removing or changing referenced documents', () => {
    const dummyData = getPreparedFixture('lksg-all-fields', preparedFixtures).t;
    mountFormWithPrefilledData(dummyData).then(() => {
      cy.get('[data-test^="BaseDataPointFormField"] button[data-test="files-to-upload-remove"]', {
        timeout: mediumTimeoutInMs,
      })
        .first()
        .parents('[data-test^="BaseDataPointFormField"]')
        .first()
        .find('input[type="checkbox"]')
        .eq(1)
        .click()
        .find('button[data-test="files-to-upload-remove"]')
        .should('not.exist');
    });
  });

  it('Prefilled form should work properly changing subcontracting companies', () => {
    const dummyData = getPreparedFixture('lksg-with-subcontracting-countries', preparedFixtures).t;
    mountFormWithPrefilledData(dummyData).then(() => {
      cy.get('[data-test="subcontractingCompaniesCountries"]', {
        timeout: mediumTimeoutInMs,
      }).within(() => {
        cy.get('.p-multiselect').first().should('contains.text', 'Germany');
        cy.get('.p-multiselect').first().should('contains.text', 'United Kingdom');
        cy.get('.p-multiselect').first().click();
        cy.get('h5:contains("Subcontracting Companies Industries in Germany")')
          .parents('.form-field')
          .first()
          .find('.d-nace-chipview')
          .children()
          .should('have.length', 2);
        cy.get('h5:contains("Subcontracting Companies Industries in United Kingdom")')
          .parents('.form-field')
          .first()
          .find('.d-nace-chipview')
          .children()
          .should('have.length', 1);
      });
      cy.get('h5:contains("Subcontracting Companies Industries in Albania")').should('not.exist');
      cy.get('[data-pc-name="multiselect"]').get('[data-pc-section="list"]').contains('Albania').click();
      cy.get('h5:contains("Subcontracting Companies Industries in Albania")').should('exist');
      cy.intercept('**/api/data/lksg*', (request) => {
        const body = request.body as CompanyAssociatedDataLksgData;
        expect(body.data.general.productionSpecific?.subcontractingCompaniesCountries).to.deep.equal({
          DE: ['A', 'G'],
          GB: ['B'],
          AL: [],
        });
        request.reply(200);
      }).as('send');
      cy.get('.p-multiselect-overlay').invoke('hide');
      submitButton.clickButton();
      cy.wait('@send');
    });
  });
});

/**
 * Function to mount the lksg upload form pre-filled with the provided data
 * @param data the data to prefill the form with
 * @returns the mounted component
 */
function mountFormWithPrefilledData(data: LksgData): Cypress.Chainable {
  const dummyCompanyAssociatedData: CompanyAssociatedDataLksgData = {
    companyId: 'company-id',
    reportingPeriod: '2024',
    data: data,
  };

  return getMountingFunction({ keycloak: minimalKeycloakMock() })(CreateLksgDataset, {
    props: {
      companyID: 'company-id',
    },
    data() {
      return { companyAssociatedLksgData: dummyCompanyAssociatedData };
    },
  });
}
