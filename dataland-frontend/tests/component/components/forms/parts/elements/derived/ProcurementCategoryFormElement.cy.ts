import ProcurementCategoryFormElement from "@/components/forms/parts/elements/derived/ProcurementCategoryFormElement.vue";

describe("Component tests for the CreateLksgDataset that test dependent fields", () => {
  it("On the upload page, ensure that procurementCategories is displayed correctly", () => {
    cy.mountWithPlugins(ProcurementCategoryFormElement, {
      data() {
        return {
          isActive: true,
          selectedCountries: [
            //TODO check why this doesn't work
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
      cy.get('[name="shareOfTotalProcurementInPercent"]').type("133").blur();
      cy.get(".formkit-message").should("contain.text", "must be between 0 and 100");
      cy.get('[name="shareOfTotalProcurementInPercent"]').clear().type("22");

      cy.get('[data-test="suppliersPerCountryCode"] .p-multiselect').click();
      cy.get("li").contains("American Samoa (AS)").click();
      cy.get("li").contains("Andorra (AD)").click();
      cy.get("li").contains("Germany (DE)").click();
      cy.pause();
      cy.get('[data-test="supplierCountry"]').should("have.length", 3);
      cy.get('[data-test="supplierCountry"]').find('[data-test="removeElementBtn"]').eq(1).click();
      cy.get('[data-test="supplierCountry"]').should("have.length", 2);
    });
  });
});
