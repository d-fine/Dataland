import LksgSubcontractingCompaniesFormField from "@/components/forms/parts/fields/LksgSubcontractingCompaniesFormField.vue";

describe("Component tests for the LksgSubcontractingCompaniesFormField", () => {
  it("Ensure that NACE codes can be selected for selected countries", () => {
    cy.mountWithPlugins(LksgSubcontractingCompaniesFormField, {}).then(() => {
      cy.get('[data-pc-name="multiselect"]').should("be.visible");
      cy.get('[data-pc-name="multiselect"]').click();
      cy.get('[data-pc-name="multiselect"]').should("be.visible");
      cy.get('[data-pc-name="multiselect"]')
        .get('[data-pc-section="wrapper"]')
        .get('[data-pc-section="list"]')
        .find("li")
        .should("contain", "Albania")
        .get('[aria-label="Albania (AL)"]')
        .should("contain", "Albania (AL)")
        .click();
       cy.get('[data-test="NaceCodeSelectorInput"]').should("be.visible");
      cy.get('[data-test="NaceCodeSelectorInput"]').click()
      cy.get('[data-test="NaceCodeSelectorTree"]').find("li").eq(0).should("contain", "A - AGRICULTURE, FORESTRY AND FISHING")
          .get('[data-test="NaceCodeSelectorCheckbox"]').click();

          cy.get('[data-test="NaceCodeSelectorInput"]').type("62.02");
      cy.get('[data-test="NaceCodeSelectorTree"]')
        .find("li")
        .should("have.length", 4)
        .eq(3)
        .should("contain", "Computer consultancy activities").click();

    });
  });
});
