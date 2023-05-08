import CreateLksgDataset from "@/components/forms/CreateLksgDataset.vue";
describe("Component tests for the CreateLksgDataset that test dependent fields", () => {
  it("On the upload page, ensure that capacity is only shown and in the data model when manufacturing company is set to yes", () => {
    cy.mountWithPlugins(CreateLksgDataset, {});
    cy.get("input[name=capacity]").should("not.exist");
    cy.get("input[id=manufacturingCompany-option-yes]").click();
    cy.get("input[name=capacity]").should("be.visible").type("5000");
    // TODO: Finish this test
  });
});
