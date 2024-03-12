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
      cy.get('[data-test="NaceCodeSelectorInput"]').click();
      cy.get('[data-test="NaceCodeSelectorInput"]').type("01.11");
      cy.get('[data-test="NaceCodeSelectorTree"]')
        .find("li")
        .should("have.length", 4)
        .eq(3)
        .should("contain", "Growing of cereals (except rice), leguminous crops and oil seeds")
        .get('[data-pc-section="label"]')
        .get('[data-test="NaceCodeSelectorCheckbox"]')
        .last()
        .click();
      cy.get("em").should("contain", "close");
    });
  });
});
