import ProcurementCategoryFormElement from "@/components/forms/parts/elements/derived/ProcurementCategoryFormElement.vue";

describe("Component tests for the CreateLksgDataset that test dependent fields", () => {
  it("On the upload page, ensure that procurementCategories is displayed correctly", () => {
    cy.mountWithPlugins(ProcurementCategoryFormElement, {
      data() {
        return {
          isItActive: true,
          selectedCountries: [
            { label: "American Samoa (AS)", value: "AS" },
            { label: "Andorra (AD)", value: "AD" },
            { label: "Germany (DE)", value: "DE" },
          ],
        };
      },
    }).then(() => {
      cy.get('[data-test="ProcurementCategoryFormElementContent"]').should("be.visible");
      cy.get('[data-test="dataPointToggleButton"]').click();
      cy.get('[data-test="ProcurementCategoryFormElementContent"]').should("not.exist");
      cy.get('[data-test="dataPointToggleButton"]').click();

      cy.get('[data-test="suppliersPerCountryCode"] .p-multiselect').click();

      cy.get('[data-test="supplierCountry"]').should("have.length", 3);
      cy.get('[data-test="supplierCountry"]').find('[data-test="removeElementBtn"]').eq(1).click();
      cy.get('[data-test="supplierCountry"]').should("have.length", 2);
    });
  });
});
