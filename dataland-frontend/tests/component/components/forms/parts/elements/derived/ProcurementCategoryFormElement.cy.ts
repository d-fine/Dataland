// @ts-nocheck
import ProcurementCategoryFormElement from '@/components/forms/parts/elements/derived/ProcurementCategoryFormElement.vue';

describe('Component tests for the CreateLksgDataset that test dependent fields', () => {
  it('On the upload page, ensure that procurementCategories is displayed correctly', () => {
    cy.mountWithPlugins(ProcurementCategoryFormElement, {
      global: {
        provide: {
          selectedProcurementCategories: {
            Products: {
              procuredProductTypesAndServicesNaceCodes: ['naceCode1'],
              numberOfSuppliersPerCountryCode: { AL: 4, AU: 2, DE: 6 },
              shareOfTotalProcurementInPercent: 72,
            },
          },
        },
      },
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: { name: 'Products' },
    }).then(() => {
      cy.get('[data-test="ProcurementCategoryFormElementContent"]').should('be.visible');
      cy.get('[data-test="dataPointToggleButton"]').click();
      cy.get('[data-test="ProcurementCategoryFormElementContent"]').should('not.exist');
      cy.get('[data-test="dataPointToggleButton"]').click();
      cy.get('[name="shareOfTotalProcurementInPercent"]').type('133').blur();
      cy.get('.formkit-message').should('contain.text', 'must be between 0 and 100');
      cy.get('[name="shareOfTotalProcurementInPercent"]').clear().type('22');

      cy.get('[data-test="suppliersPerCountryCode"] .p-multiselect').click();

      cy.get('[data-test="supplierCountry"]').should('have.length', 3);
      cy.get('[data-test="supplierCountry"]').find('[data-test="removeElementBtn"]').eq(1).click();
      cy.get('[data-test="supplierCountry"]').should('have.length', 2);
    });
  });
});
