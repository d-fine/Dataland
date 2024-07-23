// @ts-nocheck
import CreateLksgDataset from '@/components/forms/CreateLksgDataset.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { type CompanyAssociatedDataLksgData, type LksgData } from '@clients/backend';
import { submitButton } from '@sharedUtils/components/SubmitButton';

describe('Test YesNoBaseDataPointFormField for entries', () => {
  let preparedFixtures: Array<FixtureData<LksgData>>;
  before(() => {
    cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
    });
  });

  it('Edit and subsequent upload should work properly when removing or changing referenced documents', () => {
    const dummyData = getPreparedFixture('lksg-all-fields', preparedFixtures).t;
    mountEditForm(dummyData).then(() => {
      cy.get("[data-test^='BaseDataPointFormField'] button[data-test='files-to-upload-remove']")
        .first()
        .parents('[data-test^="BaseDataPointFormField"]')
        .first()
        .find('input.p-radiobutton')
        .eq(1)
        .click()
        .find("button[data-test='files-to-upload-remove']")
        .should('not.exist');
    });
  });

  it('Edit and subsequent upload should work properly changing subcontracting companies', () => {
    const dummyData = getPreparedFixture('lksg-with-subcontracting-countries', preparedFixtures).t;
    mountEditForm(dummyData).then(() => {
      cy.get("[data-test='subcontractingCompaniesCountries']", {
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      }).within(() => {
        cy.get('.p-multiselect').first().should('contains.text', 'Germany');
        cy.get('.p-multiselect').first().should('contains.text', 'United Kingdom');
        cy.get('.p-multiselect').first().click();
        cy.get("h5:contains('Subcontracting Companies Industries in Germany')")
          .parents('.form-field')
          .first()
          .find('.d-nace-chipview')
          .children()
          .should('have.length', 2);
        cy.get("h5:contains('Subcontracting Companies Industries in United Kingdom')")
          .parents('.form-field')
          .first()
          .find('.d-nace-chipview')
          .children()
          .should('have.length', 1);
      });
      cy.get("h5:contains('Subcontracting Companies Industries in Albania')").should('not.exist');
      cy.get('[data-pc-name="multiselect"]')
        .get('[data-pc-section="wrapper"]')
        .get('[data-pc-section="list"]')
        .find("li:contains('Albania')")
        .click();
      cy.get("h5:contains('Subcontracting Companies Industries in Albania')").should('exist');
      cy.intercept('**/api/data/lksg*', (request) => {
        const body = request.body as CompanyAssociatedDataLksgData;
        expect(body.data.general.productionSpecific?.subcontractingCompaniesCountries).to.deep.equal({
          DE: ['A', 'G'],
          GB: ['B'],
          AL: [],
        });
        request.reply(200);
      }).as('send');
      submitButton.clickButton();
      cy.wait('@send');
    });
  });
});

/**
 * Function to mount the lksg upload form in edit mode with the provided data
 * @param data the data to prefill the form with
 * @returns the mounted component
 */
function mountEditForm(data: LksgData): Cypress.Chainable {
  const dummyCompanyAssociatedData: CompanyAssociatedDataLksgData = {
    companyId: 'company-id',
    reportingPeriod: '2024',
    data: data,
  };
  cy.intercept('**/api/data/lksg/*', dummyCompanyAssociatedData);
  return cy.mountWithPlugins(CreateLksgDataset, {
    keycloak: minimalKeycloakMock({}),
    data: () => ({
      route: {
        query: {
          templateDataId: 'data-id',
        },
      },
    }),
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    props: {
      companyID: 'company-id',
    },
  });
}
