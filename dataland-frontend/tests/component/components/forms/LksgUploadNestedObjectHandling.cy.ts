import CreateLksgDataset from "@/components/forms/CreateLksgDataset.vue";
describe("Component tests for the CreateLksgDataset that test dependent fields", () => {
  it("On the upload page, ensure that capacity is only shown and in the data model when manufacturing company is set to yes", () => {
    cy.mountWithPlugins(CreateLksgDataset, {}).then((mounted) => {
      cy.get("input[name=capacity]").should("not.exist");
      cy.get("input[id=manufacturingCompany-option-yes]").click();
      cy.get("input[name=capacity]").should("be.visible").type("5000");
      cy.wrap(mounted.component)
        .its("companyAssociatedLksgData.data.general.productionSpecific.capacity")
        .should("eq", "5000");
      cy.get("input[id=manufacturingCompany-option-no]").click();
      cy.wrap(mounted.component)
        .its("companyAssociatedLksgData.data.general.productionSpecific")
        .its("capacity")
        .should("not.exist");
    });
  });
});
